package dev.enro.handle

import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.RootContext
import dev.enro.platform.desktop.RootWindow
import dev.enro.result.NavigationResult
import dev.enro.result.NavigationResultChannel
import dev.enro.ui.destinations.RootWindowDestination
import dev.enro.ui.destinations.isRootContextDestination


internal actual fun <T : NavigationKey> RootNavigationHandle<T>.handleNavigationOperationForPlatform(
    operation: NavigationOperation,
    context: RootContext,
): Boolean {
    val window = requireNotNull(context.parent as? RootWindow<NavigationKey>) {
        "The context parent must be a RootWindow. Found: ${context.parent::class.simpleName}"
    }
    val operations = when(operation) {
        is NavigationOperation.AggregateOperation -> operation.operations
        else -> listOf(operation)
    }
    val close = operations
        .filterIsInstance<NavigationOperation.Close<*>>()
        .firstOrNull { it.instance.id == instance.id }

    val complete = operations.filterIsInstance<NavigationOperation.Complete<*>>()
        .firstOrNull { it.instance.id == instance.id }

    val opens = operations.filterIsInstance<NavigationOperation.Open<*>>()
        .filter {
            it.instance.isRootContextDestination(context.controller)
        }

    if (opens.isEmpty() && close == null && complete == null) return false
    opens.forEach {
        RootWindowDestination.openAsRootWindow(context, it.instance)
    }
    when {
        complete != null -> {
            NavigationResultChannel.registerResult(
                NavigationResult.Completed(instance, complete.result),
            )
            context.controller.rootContextRegistry.unregister(window.navigationContext)
        }
        close != null -> {
            if (!close.silent) {
                NavigationResultChannel.registerResult(
                    NavigationResult.Closed(instance),
                )
            }
            context.controller.rootContextRegistry.unregister(window.navigationContext)
        }
        else -> {}
    }
    return true
}