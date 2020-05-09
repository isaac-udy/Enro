package nav.enro.core.internal.handle

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import nav.enro.core.internal.addOnBackPressedListener
import nav.enro.core.internal.onEvent
import nav.enro.core.*
import nav.enro.core.context.*
import nav.enro.core.context.ActivityContext
import nav.enro.core.context.FragmentContext
import nav.enro.core.context.NavigationContext
import nav.enro.core.context.leafContext
import nav.enro.core.controller.NavigationController
import nav.enro.core.internal.navigationHandle

internal class NavigationHandleViewModel<T : NavigationKey> : ViewModel(), NavigationHandle<T> {

    private var pendingInstruction: NavigationInstruction? = null

    private var internalOnCloseRequested: () -> Unit = { close() }

    override lateinit var key: T
    override lateinit var id: String
    override lateinit var controller: NavigationController
    override lateinit var additionalData: Bundle

    private val lifecycle = LifecycleRegistry(this).apply {
        addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if(event == Lifecycle.Event.ON_CREATE) controller.onOpened(this@NavigationHandleViewModel)
                if(event == Lifecycle.Event.ON_DESTROY) controller.onClosed(this@NavigationHandleViewModel)
            }
        })
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycle
    }

    internal var navigationContext: NavigationContext<*, T>? = null
        set(value) {
            field?.let {
                val id = it.instruction?.id ?: return@let
                it.controller.handles.remove(id)
            }
            field = value
            value?.let {
                controller = it.controller
                key = it.key
                additionalData = it.instruction?.additionalData ?: Bundle()
                id = it.instruction?.id ?: return@let
                it.controller.handles[id] = this
            }
            if (value == null) return
            registerLifecycleObservers(value)
            registerOnBackPressedListener(value)
            executePendingInstruction()

            if(lifecycle.currentState == Lifecycle.State.INITIALIZED) {
                lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            }
        }


    private fun registerLifecycleObservers(context: NavigationContext<out Any, T>) {
        context.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if(event == Lifecycle.Event.ON_DESTROY || event == Lifecycle.Event.ON_CREATE) return
                lifecycle.handleLifecycleEvent(event)
            }
        })
        context.lifecycle.onEvent(Lifecycle.Event.ON_DESTROY) {
            if (context == navigationContext) navigationContext = null
        }
    }

    private fun registerOnBackPressedListener(context: NavigationContext<out Any, T>) {
        if (context is ActivityContext<out FragmentActivity, *>) {
            context.activity.addOnBackPressedListener {
                context.leafContext().navigationHandle().internalOnCloseRequested()
            }
        }
    }

    override fun executeInstruction(navigationInstruction: NavigationInstruction) {
        pendingInstruction = navigationInstruction
        executePendingInstruction()
    }

    override fun onCloseRequested(onCloseRequested: () -> Unit) {
        internalOnCloseRequested = onCloseRequested
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
            is NavigationInstruction.Open<*> -> {
                context.controller.open(context, instruction)
            }
        }
    }

    internal fun executeDeeplink() {
        val context = navigationContext ?: TODO("Nice Exception")

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