package dev.enro3.handle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleRegistry
import dev.enro3.platform.EnroLog
import dev.enro3.NavigationContext
import dev.enro3.NavigationHandle
import dev.enro3.NavigationKey
import dev.enro3.NavigationOperation

internal class NavigationHandleImpl<T : NavigationKey>(
    override val instance: NavigationKey.Instance<T>
) : NavigationHandle<T>() {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry

    private var context: NavigationContext<T>? = null

    private val lifecycleObserver = LifecycleEventObserver { owner, event ->
        when (event) {
            Lifecycle.Event.ON_START -> lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            Lifecycle.Event.ON_STOP -> lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            Lifecycle.Event.ON_RESUME -> lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            Lifecycle.Event.ON_PAUSE -> lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            Lifecycle.Event.ON_CREATE -> {
                // No op: ON_CREATE is handled through the bindContext function
            }
            Lifecycle.Event.ON_DESTROY -> {
                // No op: ON_DESTROY is handled through the onDestroy function
            }
            Lifecycle.Event.ON_ANY -> {
                // No op
            }
        }
    }

    internal fun bindContext(context: NavigationContext<T>) {
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) return

        this.context?.lifecycle?.removeObserver(lifecycleObserver)
        this.context = context
        if (lifecycle.currentState == Lifecycle.State.INITIALIZED) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        }
        this.context?.lifecycle?.addObserver(lifecycleObserver)
    }

    internal fun onDestroy() {
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) return
        context?.lifecycle?.removeObserver(lifecycleObserver)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        context = null
    }

    override fun execute(operation: NavigationOperation) {
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) return
        val context = context
        if (context == null) {
            EnroLog.warn("NavigationHandle with instance $instance has no context")
            return
        }
        context.parentContainer.execute(operation)
    }
}