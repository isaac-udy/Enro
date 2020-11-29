package nav.enro.core.internal.handle

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import nav.enro.core.*
import nav.enro.core.context.*
import nav.enro.core.controller.NavigationController
import nav.enro.core.internal.addOnBackPressedListener
import nav.enro.core.internal.navigationHandle
import nav.enro.core.internal.onEvent

internal class NavigationHandleViewModelFactory(
    private val navigationController: NavigationController,
    private val instruction: NavigationInstruction.Open
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NavigationHandleViewModel(
            navigationController,
            instruction
        ) as T
    }
}

internal fun ViewModelStoreOwner.createNavigationHandleViewModel(
    navigationController: NavigationController,
    instruction: NavigationInstruction.Open
): NavigationHandleViewModel {
    return when(this) {
        is FragmentActivity -> viewModels<NavigationHandleViewModel> {
            NavigationHandleViewModelFactory(navigationController, instruction)
        }.value
        is Fragment -> viewModels<NavigationHandleViewModel> {
            NavigationHandleViewModelFactory(navigationController, instruction)
        }.value
        else -> throw IllegalArgumentException("ViewModelStoreOwner must be a Fragment or Activity")
    }
}

internal fun ViewModelStoreOwner.getNavigationHandleViewModel(): NavigationHandleViewModel {
    return when(this) {
        is FragmentActivity -> viewModels<NavigationHandleViewModel> { ViewModelProvider.NewInstanceFactory() }.value
        is Fragment -> viewModels<NavigationHandleViewModel> { ViewModelProvider.NewInstanceFactory() }.value
        else -> throw IllegalArgumentException("ViewModelStoreOwner must be a Fragment or Activity")
    }
}

internal class NavigationHandleViewModel(
    override val controller: NavigationController,
    internal val instruction: NavigationInstruction.Open
) : ViewModel(), NavigationHandle {

    private var pendingInstruction: NavigationInstruction? = null

    internal val hasKey get() = instruction.navigationKey !is NoNavigationKeyBound

    override val key: NavigationKey get() {
        if(instruction.navigationKey is NoNavigationKeyBound) throw IllegalStateException("This NavigationHandle has no NavigationKey")
        return instruction.navigationKey
    }
    override val id: String get() = instruction.instructionId
    override val additionalData: Bundle get() = instruction.additionalData

    internal var childContainers = listOf<ChildContainer>()
    internal var internalOnCloseRequested: () -> Unit = { close() }

    private val lifecycle = LifecycleRegistry(this).apply {
        addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_CREATE) controller.onOpened(this@NavigationHandleViewModel)
                if (event == Lifecycle.Event.ON_DESTROY) controller.onClosed(this@NavigationHandleViewModel)
            }
        })
    }

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

    init {
        controller.handles[id] = this
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
                context.leafContext().navigationHandle().internalOnCloseRequested()
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
            NavigationInstruction.Open(
                NavigationDirection.FORWARD,
                instruction.children.first(),
                instruction.children.drop(1)
            )
        )
    }

    override fun onCleared() {
        controller.handles.remove(id)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}