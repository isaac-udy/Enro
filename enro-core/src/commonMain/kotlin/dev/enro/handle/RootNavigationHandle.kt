package dev.enro.handle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.RootContext
import dev.enro.platform.EnroLog
import dev.enro.result.NavigationResultChannel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class RootNavigationHandle<T : NavigationKey>(
    instance: NavigationKey.Instance<T>,
    override val savedStateHandle: SavedStateHandle,
) : NavigationHandle<T>() {
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry

    internal var context: RootContext? = null
        private set

    override var instance: NavigationKey.Instance<T> = instance
        private set

    private val lifecycleObserver = LifecycleEventObserver { owner, event ->
        when (event) {
            Lifecycle.Event.ON_START -> lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            Lifecycle.Event.ON_STOP -> lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            Lifecycle.Event.ON_RESUME -> lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            Lifecycle.Event.ON_PAUSE -> lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            Lifecycle.Event.ON_DESTROY -> {
                context = null
            }
            Lifecycle.Event.ON_CREATE -> {
                // No op: ON_CREATE is handled through the bindContext function
            }
            Lifecycle.Event.ON_ANY -> {
                // No op
            }
        }
    }

    init {
        NavigationResultChannel.completedFromSignalFor(instance)
            .onEach {
                execute(NavigationOperation.Close(instance = instance, silent = true))
            }
            .launchIn(lifecycleScope)
    }

    internal fun bindContext(context: RootContext) {
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) return
        this.context?.lifecycle?.removeObserver(lifecycleObserver)
        this.context = context
        if (lifecycle.currentState == Lifecycle.State.INITIALIZED) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        }
        this.context?.lifecycle?.addObserver(lifecycleObserver)
        context.controller.rootContextRegistry.register(context)
    }

    internal fun onDestroy() {
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) return
        context?.let { context ->
            this.context = null
            context.lifecycle.removeObserver(lifecycleObserver)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            context.controller.rootContextRegistry.unregister(context)
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
        val wasHandledByPlatform = handleNavigationOperationForPlatform(
            operation = operation,
            context = context,
        )
        if (wasHandledByPlatform) return
        val containerContext = findContainerForOperation(
            fromContext = context,
            operation = operation,
        )
        requireNotNull(containerContext) {
            "Could not find a valid container for the navigation operation: $operation from RootContext ${context.parent}"
        }
        containerContext
            .container
            .execute(context, operation)
    }
}

/**
 * Handles navigation operations using platform-specific implementations for RootContexts.
 * 
 * This function allows RootContexts to handle certain navigation operations with platform-specific
 * logic. For example, on Android where the RootContext type is Activity, this function handles
 * operations like close or complete by calling Activity.finish() or Activity.setResult() respectively.
 * 
 * @return true if the operation was handled by platform-specific logic, false otherwise
 */
internal expect fun <T: NavigationKey> RootNavigationHandle<T>.handleNavigationOperationForPlatform(
    operation: NavigationOperation,
    context: RootContext,
): Boolean