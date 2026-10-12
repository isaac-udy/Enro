package dev.enro.handle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.DestinationContext
import dev.enro.context.root
import dev.enro.platform.EnroLog

internal class DestinationNavigationHandle<T : NavigationKey>(
    instance: NavigationKey.Instance<T>,
    override val savedStateHandle: SavedStateHandle,
) : NavigationHandle<T>() {
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry

    private var context: DestinationContext<T>? = null
    private var attachedContext: DestinationContext<T>? = null
    private val pendingOperations = mutableListOf<NavigationOperation>()
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
        if (this.context === context) return
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) return
        require(context.destination.instance.id == instance.id) {
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

    /**
     * Marks [context] as registered in the navigation tree, along with any containers its content
     * declares, and runs the operations that were held while no registered context was available.
     */
    internal fun attachContext(context: DestinationContext<T>) {
        if (this.context !== context) return
        attachedContext = context
        val operations = pendingOperations.toList()
        pendingOperations.clear()
        operations.forEach { execute(it) }
    }

    internal fun onDestroy() {
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) return
        pendingOperations.clear()
        attachedContext = null
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
        // Held until attachContext while the first composition has not registered this context and its
        // containers, or while the context's host is destroyed (e.g. an Activity being recreated).
        val isContextUsable = context != null &&
            context === attachedContext &&
            context.root().lifecycle.currentState != Lifecycle.State.DESTROYED
        if (!isContextUsable) {
            pendingOperations.add(operation)
            return
        }
        val isInBackstack = context.parent.container.backstack.any { it.id == context.destination.instance.id }
        if (!isInBackstack) {
            // Some destinations (particularly overlay destinations that have animations) may not enter the
            // DESTROYED state immediately after being removed from their parent container, so they may still
            // receive NavigationOperations. An example is a Dialog's scrim being tapped multiple times during the
            // dismiss animation. In these cases, we want to ignore the operations. We print a warning here,
            // so that it's visible to developers, but this should not necessarily raise concerns.
            EnroLog.warn(
                "NavigationHandle with instance $instance is not in its parent's backstack, ignoring operation: $operation. " +
                    "If this follows a close() from the same destination, use closeAndReplaceWith(...) to replace " +
                    "the destination in one operation."
            )
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