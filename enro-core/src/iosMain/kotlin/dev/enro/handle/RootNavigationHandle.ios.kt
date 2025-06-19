package dev.enro.handle

import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.RootContext
import dev.enro.result.NavigationResult
import dev.enro.result.NavigationResultChannel
import dev.enro.ui.destinations.UIViewControllerDestination
import dev.enro.ui.destinations.isRootContextDestination
import platform.UIKit.UINavigationController
import platform.UIKit.UIViewController

internal actual fun <T : NavigationKey> RootNavigationHandle<T>.handleNavigationOperationForPlatform(
    operation: NavigationOperation,
    context: RootContext,
): Boolean {
    val uiViewController = requireNotNull(context.parent as? UIViewController) {
        "The context parent must be a EnroUINavigationController. Found: ${context.parent::class.simpleName}"
    }
    val uiNavigationController = findUINavigationController(uiViewController)

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
    val configurations = opens.mapNotNull {
        val configuration = UIViewControllerDestination.getConfiguration(
            controller = context.controller,
            instance = it.instance,
        )
        if (configuration == null) return@mapNotNull null
        it.instance to configuration
    }
    configurations.forEach { (key, configuration) ->
        UIViewControllerDestination.executePresentationAction(
            configuration = configuration,
            instance = key,
            uiViewController = uiViewController,
            uiNavigationController = uiNavigationController
        )
    }
    when {
        complete != null -> {
            NavigationResultChannel.registerResult(
                NavigationResult.Completed(instance, complete.result),
            )
        }
        close != null -> {
            if (!close.silent) {
                NavigationResultChannel.registerResult(
                    NavigationResult.Closed(instance),
                )
            }
        }
        else -> {}
    }
    if (close != null || complete != null) {
        if (uiNavigationController != null) {
            uiNavigationController.setViewControllers(
                uiNavigationController.viewControllers.filter { it != uiViewController },
                animated = true
            )
        }
        else {
            uiViewController.presentingViewController
                ?.dismissViewControllerAnimated(true, null)
        }
    }
    return true
}

private fun findUINavigationController(from: UIViewController): UINavigationController? {
    var current: UIViewController? = from
    while (current != null) {
        if (current is UINavigationController) {
            return current
        }
        current = current.parentViewController
    }
    return null
}