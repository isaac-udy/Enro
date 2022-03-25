package dev.enro.core.internal.handle

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.*
import dev.enro.core.*
import dev.enro.core.controller.NavigationController
import dev.enro.core.internal.NoNavigationKey

internal open class NavigationHandleViewModel(
    override val controller: NavigationController,
    internal val instruction: NavigationInstruction.Open
) : ViewModel(), NavigationHandle {

    private var pendingInstruction: NavigationInstruction? = null

    internal val hasKey get() = instruction.navigationKey !is NoNavigationKey

    override val key: NavigationKey get() {
        if(instruction.navigationKey is NoNavigationKey) throw IllegalStateException(
            "The navigation handle for the context ${navigationContext?.contextReference} has no NavigationKey"
        )
        return instruction.navigationKey
    }
    override val id: String get() = instruction.instructionId
    override val additionalData: Bundle get() = instruction.additionalData

    internal var internalOnCloseRequested: () -> Unit = { close() }

    private val lifecycle = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle {
        return lifecycle
    }

    internal var navigationContext: NavigationContext<*>? = null
        set(value) {
            field = value
            if (value == null) return

            registerLifecycleObservers(value)
            registerOnBackPressedListener(value)
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

    private fun registerOnBackPressedListener(context: NavigationContext<out Any>) {
        if (context is ActivityContext<out ComponentActivity>) {
            context.activity.addOnBackPressedListener {
                context.leafContext().getNavigationHandleViewModel().requestClose()
            }
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
        val execute: () -> Unit = {
            when (instruction) {
                is NavigationInstruction.Open -> {
                    context.controller.open(context, instruction)
                }
                NavigationInstruction.RequestClose -> {
                    internalOnCloseRequested()
                }
                NavigationInstruction.Close -> context.controller.close(context)
            }
        }

        val isMainLooper = Looper.getMainLooper() == Looper.myLooper()
        if(isMainLooper && context.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            execute()
        }
        else {
            context.lifecycleOwner.lifecycleScope.launchWhenCreated {
                execute()
            }
        }
    }

    internal fun executeDeeplink() {
        if (instruction.children.isEmpty()) return
        executeInstruction(
            NavigationInstruction.Forward(
                navigationKey = instruction.children.first(),
                children = instruction.children.drop(1)
            )
        )
    }

    override fun onCleared() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
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

private fun ComponentActivity.addOnBackPressedListener(block: () -> Unit) {
    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            block()
        }
    })
}