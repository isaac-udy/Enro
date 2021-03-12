package dev.enro.core.internal.handle

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import nav.enro.core.*
import nav.enro.core.controller.NavigationController
import nav.enro.core.internal.NoNavigationKey

internal class NavigationHandleViewModel(
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

    internal var childContainers = listOf<ChildContainer>()
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
        if (context is ActivityContext<out FragmentActivity>) {
            context.activity.addOnBackPressedListener {
                context.leafContext().getNavigationHandleViewModel().internalOnCloseRequested()
            }
        }
    }

    override fun executeInstruction(navigationInstruction: NavigationInstruction) {
        pendingInstruction = navigationInstruction
        executePendingInstruction()
    }

    private fun executePendingInstruction() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            Handler(Looper.getMainLooper()).post { executePendingInstruction() }
            return
        }
        val context = navigationContext ?: return
        val instruction = pendingInstruction ?: return
        pendingInstruction = null

        when (instruction) {
            NavigationInstruction.Close -> context.controller.close(context.leafContext())
            is NavigationInstruction.Open -> {
                context.controller.open(context, instruction)
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

private fun FragmentActivity.addOnBackPressedListener(block: () -> Unit) {
    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            block()
        }
    })
}