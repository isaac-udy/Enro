package dev.enro.handle

import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.RootContext
import dev.enro.ui.destinations.isRootContextDestination
import platform.UIKit.UIViewController

internal actual fun <T : NavigationKey> RootNavigationHandle<T>.handleNavigationOperationForPlatform(
    operation: NavigationOperation,
    context: RootContext,
): Boolean {
    val uiViewController = requireNotNull(context.parent as? UIViewController) {
        "The context parent must be a EnroUINavigationController. Found: ${context.parent::class.simpleName}"
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
    TODO("Need to implement root-level navigation for iOS")
//    opens.forEach {
//        RootWindowDestination.openAsRootWindow(context, it.instance)
//    }
//    when {
//        complete != null -> {
//            NavigationResultChannel.registerResult(
//                NavigationResult.Completed(instance, complete.result),
//            )
//            context.controller.rootContextRegistry.unregister(uIViewController.context)
//        }
//        close != null -> {
//            if (!close.silent) {
//                NavigationResultChannel.registerResult(
//                    NavigationResult.Closed(instance),
//                )
//            }
//            context.controller.rootContextRegistry.unregister(uIViewController.context)
//        }
//        else -> {}
//    }
    return true
}