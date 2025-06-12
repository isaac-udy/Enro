package dev.enro.handle

import android.app.Activity
import android.content.Intent
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.RootContext
import dev.enro.platform.activity
import dev.enro.result.NavigationResult
import dev.enro.result.NavigationResultChannel
import dev.enro.ui.destinations.ActivityTypeKey
import dev.enro.ui.destinations.putNavigationKeyInstance
import kotlin.reflect.KClass

internal actual fun <T : NavigationKey> RootNavigationHandle<T>.handleNavigationOperationForPlatform(
    operation: NavigationOperation,
    context: RootContext,
): Boolean {
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
        .mapNotNull {
            val activityType = context.controller.bindings
                .bindingFor(it.instance)
                .provider
                .peekMetadata(it.instance)
                .get(ActivityTypeKey)

            when (activityType) {
                is KClass<*> -> it.instance to activityType
                else -> null
            }
        }

    if (opens.isEmpty() && close == null && complete == null) return false
    val activity = context.activity
    val intents = opens.map { (instance, type) ->
        Intent(activity, type.java).putNavigationKeyInstance(instance)
    }
    if (intents.isNotEmpty()) {
        // TODO for result!
        activity.startActivities(intents.toTypedArray())
    }
    when {
        complete != null -> {
            activity.setResult(Activity.RESULT_OK, resultFromEnro())
            NavigationResultChannel.registerResult(
                NavigationResult.Completed(instance, complete.result),
            )
            activity.finish()
        }
        close != null -> {
            if (!close.silent) {
                activity.setResult(Activity.RESULT_CANCELED, resultFromEnro())
                NavigationResultChannel.registerResult(
                    NavigationResult.Closed(instance),
                )
            }
            activity.finish()
        }
        else -> {}
    }
    return true
}

private const val RESULT_FROM_ENRO = "dev.enro.result.RESULT_FROM_ENRO"

internal fun resultFromEnro(): Intent {
    return Intent().apply {
        putExtra(RESULT_FROM_ENRO, true)
    }
}

@PublishedApi
internal fun Intent.isResultFromEnro(): Boolean {
    return getBooleanExtra(RESULT_FROM_ENRO, false)
}
