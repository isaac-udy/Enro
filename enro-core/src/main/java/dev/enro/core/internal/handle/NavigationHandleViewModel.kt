package dev.enro.core.internal.handle

import android.annotation.SuppressLint
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.controller.usecase.ExecuteCloseInstruction
import dev.enro.core.controller.usecase.ExecuteContainerOperationInstruction
import dev.enro.core.controller.usecase.ExecuteOpenInstruction
import dev.enro.core.controller.usecase.extras
import kotlinx.coroutines.launch

internal open class NavigationHandleViewModel(
    override val instruction: AnyOpenInstruction,
    dependencyScope: NavigationHandleScope,
    private val executeOpenInstruction: ExecuteOpenInstruction,
    private val executeCloseInstruction: ExecuteCloseInstruction,
    private val executeContainerOperationInstruction: ExecuteContainerOperationInstruction,
) : ViewModel(),
    NavigationHandle {

    private var pendingInstruction: NavigationInstruction? = null

    final override val key: NavigationKey get() = instruction.navigationKey
    final override val id: String get() = instruction.instructionId

    internal var internalOnCloseRequested: () -> Unit = { close() }
        set(value) {
            hasCustomOnRequestClose = true
            field = value
        }

    @Suppress("LeakingThis")
    @SuppressLint("StaticFieldLeak")
    private val lifecycleRegistry = LifecycleRegistry(this)

    @Suppress("LeakingThis")
    final override val dependencyScope: NavigationHandleScope = dependencyScope.bind(this)

    final override val lifecycle: Lifecycle get() {
        return lifecycleRegistry
    }

    internal var navigationContext: NavigationContext<*>? = null
        set(value) {
            field = value
            if (value == null) return

            value.bind(this)
            registerLifecycleObservers(value)
            executePendingInstruction()

            if (lifecycleRegistry.currentState == Lifecycle.State.INITIALIZED) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            }
        }

    private fun registerLifecycleObservers(context: NavigationContext<out Any>) {
        context.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY || event == Lifecycle.Event.ON_CREATE) return@LifecycleEventObserver
            lifecycleRegistry.handleLifecycleEvent(event)
        })
        context.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_DESTROY) return@LifecycleEventObserver
            navigationContext = null
        })
    }

    override fun executeInstruction(navigationInstruction: NavigationInstruction) {
        pendingInstruction = navigationInstruction
        executePendingInstruction()
    }

    private fun executePendingInstruction() {
        val context = navigationContext ?: return
        val instruction = pendingInstruction ?: return
        pendingInstruction = null
        context.runWhenContextActive {
            when (instruction) {
                is NavigationInstruction.Open<*> -> executeOpenInstruction(context, instruction)
                NavigationInstruction.RequestClose -> internalOnCloseRequested()
                is NavigationInstruction.Close -> executeCloseInstruction(context, instruction)
                is NavigationInstruction.ContainerOperation -> executeContainerOperationInstruction(context, instruction)
            }
        }
    }

    override fun onCleared() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        dependencyScope.close()
        dependencyScope.container.clear()
        navigationContext = null
    }
}

private fun NavigationContext<*>.runWhenContextActive(block: () -> Unit) {
    val isMainThread = Looper.getMainLooper() == Looper.myLooper()
    when(contextReference) {
        is Fragment -> {
            if(isMainThread && !contextReference.isStateSaved && contextReference.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                block()
            } else {
                lifecycleOwner.lifecycleScope.launch {
                    lifecycle.withStarted(block)
                }
            }
        }
        is ComponentActivity -> {
            if(isMainThread && contextReference.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                block()
            } else {
                lifecycleOwner.lifecycleScope.launch {
                    lifecycle.withStarted(block)
                }
            }
        }
        is ComposableDestination -> {
            if(isMainThread && contextReference.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                block()
            } else {
                lifecycleOwner.lifecycleScope.launch {
                    lifecycle.withStarted(block)
                }
            }
        }
    }
}

private const val CUSTOM_REQUEST_CLOSE_KEY = "dev.enro.core.internal.handle.NavigationHandleViewModel.hasCustomOnRequestClose"
internal var NavigationHandle.hasCustomOnRequestClose: Boolean
    get() {
        val extra = extras[CUSTOM_REQUEST_CLOSE_KEY] as? Boolean
        return extra == true
    }
    private set(value) {
        extras[CUSTOM_REQUEST_CLOSE_KEY] = value
    }