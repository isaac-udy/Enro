package dev.enro.core.internal.handle

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.addCallback
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import dev.enro.core.*
import dev.enro.core.controller.NavigationController
import dev.enro.core.fragment.interceptBackPressForAndroidxNavigation
import dev.enro.core.internal.NoNavigationKey

internal open class NavigationHandleViewModel(
    override val controller: NavigationController,
    override val instruction: NavigationInstruction.Open
) : ViewModel(), NavigationHandle {

    private var pendingInstruction: NavigationInstruction? = null

    internal val hasKey get() = instruction.navigationKey !is NoNavigationKey

    override val key: NavigationKey
        get() {
            if (instruction.navigationKey is NoNavigationKey) throw IllegalStateException(
                "The navigation handle for the context ${navigationContext?.contextReference} has no NavigationKey"
            )
            return instruction.navigationKey
        }
    override val id: String get() = instruction.instructionId
    override val additionalData: Bundle get() = instruction.additionalData

    internal var childContainers = listOf<ChildContainer>()
    internal var internalOnCloseRequested: () -> Unit = { close() }

    @SuppressLint("StaticFieldLeak")
    private val lifecycleRegistry = LifecycleRegistry(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    internal var navigationContext: NavigationContext<*>? = null
        set(value) {
            field = value
            if (value == null) return
            registerLifecycleObservers(value)
            registerOnBackPressedListener(value)
            executePendingInstruction()

            if (lifecycle.currentState == Lifecycle.State.INITIALIZED) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            }
        }

    private fun registerLifecycleObservers(context: NavigationContext<out Any>) {
        context.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY || event == Lifecycle.Event.ON_CREATE) return
                lifecycleRegistry.handleLifecycleEvent(event)
            }
        })
        context.lifecycle.onEvent(Lifecycle.Event.ON_DESTROY) {
            if (context == navigationContext) navigationContext = null
        }
    }

    private fun registerOnBackPressedListener(context: NavigationContext<out Any>) {
        if (context is ActivityContext<out FragmentActivity>) {
            context.activity.onBackPressedDispatcher.addCallback(this) {
                val leafContext = context.leafContext()
                if (interceptBackPressForAndroidxNavigation(this, leafContext)) return@addCallback
                leafContext.getNavigationHandleViewModel().requestClose()
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
        context.runWhenContextActive {
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
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}

private fun Lifecycle.onEvent(on: Lifecycle.Event, block: () -> Unit) {
    addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (on == event) {
                block()
            }
        }
    })
}