package dev.enro.core.internal.handle

import android.annotation.SuppressLint
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import dev.enro.core.*
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.controller.usecase.ExecuteCloseInstruction
import dev.enro.core.controller.usecase.ExecuteContainerOperationInstruction
import dev.enro.core.controller.usecase.ExecuteOpenInstruction
import dev.enro.core.internal.NoNavigationKey
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

    internal val hasKey get() = instruction.navigationKey !is NoNavigationKey
    final override val key: NavigationKey get() = instruction.navigationKey
    final override val id: String get() = instruction.instructionId

    internal var internalOnCloseRequested: () -> Unit = { close() }

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