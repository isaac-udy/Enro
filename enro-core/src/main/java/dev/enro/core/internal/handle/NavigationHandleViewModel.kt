package dev.enro.core.internal.handle

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import dev.enro.core.*
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.controller.EnroDependencyScope
import dev.enro.core.controller.usecase.ExecuteCloseInstruction
import dev.enro.core.controller.usecase.ExecuteOpenInstruction
import dev.enro.core.internal.NoNavigationKey

internal open class NavigationHandleViewModel(
    override val instruction: AnyOpenInstruction,
    dependencyScope: NavigationHandleScope,
    private val executeOpenInstruction: ExecuteOpenInstruction,
    private val executeCloseInstruction: ExecuteCloseInstruction,
) : ViewModel(),
    NavigationHandle {

    private var pendingInstruction: NavigationInstruction? = null

    internal val hasKey get() = instruction.navigationKey !is NoNavigationKey
    final override val key: NavigationKey get() = instruction.navigationKey
    final override val id: String get() = instruction.instructionId
    final override val additionalData: Bundle get() = instruction.additionalData

    internal var internalOnCloseRequested: () -> Unit = { close() }

    @Suppress("LeakingThis")
    @SuppressLint("StaticFieldLeak")
    private val lifecycle = LifecycleRegistry(this)

    @Suppress("LeakingThis")
    final override val dependencyScope: EnroDependencyScope = dependencyScope.bind(this)

    final override fun getLifecycle(): Lifecycle {
        return lifecycle
    }

    internal var navigationContext: NavigationContext<*>? = null
        set(value) {
            field = value
            if (value == null) return

            registerLifecycleObservers(value)
            executePendingInstruction()

            if (lifecycle.currentState == Lifecycle.State.INITIALIZED) {
                lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            }
        }

    private fun registerLifecycleObservers(context: NavigationContext<out Any>) {
        context.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY || event == Lifecycle.Event.ON_CREATE) return
                lifecycle.handleLifecycleEvent(event)
            }
        })
        context.lifecycle.onEvent(Lifecycle.Event.ON_DESTROY) {
            if (context == navigationContext) navigationContext = null
        }
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
            }
        }
    }

    internal fun executeDeeplink() {
        if (instruction.children.isEmpty()) return
        executeInstruction(
            NavigationInstruction.DefaultDirection(
                navigationKey = instruction.children.first(),
                children = instruction.children.drop(1)
            )
        )
    }

    override fun onCleared() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        dependencyScope.container.clear()
    }
}


private fun Lifecycle.onEvent(on: Lifecycle.Event, block: () -> Unit) {
    addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if(on == event) {
                block()
            }
        }
    })
}

private fun NavigationContext<*>.runWhenContextActive(block: () -> Unit) {
    val isMainThread = Looper.getMainLooper() == Looper.myLooper()
    when(this) {
        is FragmentContext<out Fragment> -> {
            if(isMainThread && !fragment.isStateSaved && fragment.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                block()
            } else {
                fragment.lifecycleScope.launchWhenStarted {
                    block()
                }
            }
        }
        is ActivityContext<out ComponentActivity> -> {
            if(isMainThread && contextReference.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                block()
            } else {
                contextReference.lifecycleScope.launchWhenStarted {
                    block()
                }
            }
        }
        is ComposeContext<out ComposableDestination> -> {
            if(isMainThread && contextReference.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                block()
            } else {
                contextReference.lifecycleScope.launchWhenStarted {
                    block()
                }
            }
        }
    }
}