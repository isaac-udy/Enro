package nav.enro.core.internal.handle

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import nav.enro.core.*
import nav.enro.core.context.*
import nav.enro.core.controller.NavigationController
import nav.enro.core.internal.addOnBackPressedListener
import nav.enro.core.internal.navigationHandle
import nav.enro.core.internal.onEvent

internal class NavigationHandleViewModel : ViewModel(), NavigationHandle {

    private var pendingInstruction: NavigationInstruction? = null

    internal var internalOnCloseRequested: () -> Unit = { close() }

    internal val hasKey get() = rawKey != null
    internal var rawKey: NavigationKey? = null
    internal var defaultKey: NavigationKey? = null

    override val key: NavigationKey get() {
        return rawKey
            ?: throw IllegalStateException("This NavigationHandle has no NavigationKey bound")
    }
    override lateinit var id: String
    override lateinit var controller: NavigationController
    override lateinit var additionalData: Bundle

    internal var childContainers = listOf<ChildContainer>()
        set(value) {
            field = value
            navigationContext?.childContainers = value
        }

    private val lifecycle = LifecycleRegistry(this).apply {
        addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (navigationContext?.instruction == null) return
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
            field?.let {
                val id = it.id
                it.controller.handles.remove(id)
            }
            field = value
            value?.let {
                it.childContainers = childContainers
                controller = it.controller
                additionalData = it.instruction?.additionalData ?: Bundle()
                id = it.id
                it.controller.handles[id] = this
                rawKey = it.key ?: return@let
            }
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
        val context = navigationContext ?: throw IllegalStateException("The NavigationHandle must be attached to a NavigationContext")

        if (context.pendingKeys.isEmpty()) return
        executeInstruction(
            NavigationInstruction.Open(
                NavigationDirection.FORWARD,
                context.pendingKeys.first(),
                context.pendingKeys.drop(1)
            )
        )
    }

    override fun onCleared() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}