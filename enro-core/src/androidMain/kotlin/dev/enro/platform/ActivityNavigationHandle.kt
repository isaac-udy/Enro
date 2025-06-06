package dev.enro.platform

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import dev.enro.NavigationKey
import dev.enro.result.NavigationResultChannel
import dev.enro.result.setResultCompleted
import kotlin.properties.ReadOnlyProperty

public class ActivityNavigationHandle<out T: NavigationKey>(
    internal val activity: ComponentActivity,
    public val instance: NavigationKey.Instance<T>,
) {
    public val id: String get() = instance.id
    public val key: T get() = instance.key
}

public fun ActivityNavigationHandle<NavigationKey>.close() {
    activity.setResult(Activity.RESULT_CANCELED, resultFromEnro())
    NavigationResultChannel.registerResult(instance)
    activity.finish()
}

public fun ActivityNavigationHandle<NavigationKey>.complete() {
    activity.setResult(Activity.RESULT_OK, resultFromEnro())
    instance.setResultCompleted()
    NavigationResultChannel.registerResult(instance)
    activity.finish()
}

@JvmName("completeWithoutResult")
@Deprecated(
    message = "A NavigationKey.WithResult should not be completed without a result, doing so will result in an error",
    level = DeprecationLevel.ERROR,
)
public fun <R : Any> ActivityNavigationHandle<NavigationKey.WithResult<R>>.complete() {
    error("${instance.key} is a NavigationKey.WithResult and cannot be completed without a result")
}

public fun <R : Any> ActivityNavigationHandle<NavigationKey.WithResult<R>>.complete(result: R) {
    activity.setResult(Activity.RESULT_OK, resultFromEnro())
    instance.setResultCompleted(result)
    NavigationResultChannel.registerResult(instance)
    activity.finish()
}

public inline fun <reified T : NavigationKey> ComponentActivity.navigationHandle() : ReadOnlyProperty<ComponentActivity, ActivityNavigationHandle<T>> {
    return ReadOnlyProperty { activity, _ ->
        val handle = activity.activityContextHolder.navigationHandle

        requireNotNull(handle) {
            error("No navigation handle found for ${activity::class.simpleName}")
        }

        require(handle.instance.key is T) {
            error("Expected navigation handle for ${T::class.simpleName}, but found ${handle.instance.key::class.simpleName}")
        }

        @Suppress("UNCHECKED_CAST")
        return@ReadOnlyProperty handle as ActivityNavigationHandle<T>
    }
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
