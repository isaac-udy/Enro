package dev.enro.handle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleRegistry
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.DestinationContext
import dev.enro.platform.EnroLog

internal class DestinationNavigationHandle<T : NavigationKey>(
    instance: NavigationKey.Instance<T>,
) : NavigationHandle<T>() {
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry

    private var context: DestinationContext<T>? = null
    override var instance: NavigationKey.Instance<T> = instance
        private set

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

    internal fun bindContext(context: DestinationContext<T>) {
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) return
        require(context.destination.instance.id == id) {
            "Cannot bind NavigationContext with instance ${context.destination.instance} to NavigationHandle with instance ${instance}"
        }
        this.context?.lifecycle?.removeObserver(lifecycleObserver)
        this.context = context
        this.instance = context.destination.instance
        if (lifecycle.currentState == Lifecycle.State.INITIALIZED) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        }
        this.context?.lifecycle?.addObserver(lifecycleObserver)
    }

    internal fun onDestroy() {
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) return
        context?.let { context ->
            this.context = null
            context.lifecycle.removeObserver(lifecycleObserver)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            context.controller.plugins.onClosed(this)
        }
    }

    override fun execute(
        operation: NavigationOperation,
    ) {
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) return
        val context = context
        if (context == null) {
            EnroLog.warn("NavigationHandle with instance $instance has no context")
            return
        }
        val containerContext = findContainerForOperation(
            fromContext = context.parent,
            operation = operation,
        )
        requireNotNull(containerContext) {
            "Could not find a valid container for the navigation operation: $operation from context with instance: ${context.destination.instance}"
        }
        containerContext
            .container
            .execute(context, operation)
    }
}