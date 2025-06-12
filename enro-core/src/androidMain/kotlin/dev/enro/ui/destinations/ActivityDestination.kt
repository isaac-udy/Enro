package dev.enro.ui.destinations

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import dev.enro.NavigationKey
import dev.enro.platform.getNavigationKeyInstance
import dev.enro.platform.putNavigationKeyInstance
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination
import kotlin.reflect.KClass

public inline fun <reified T : NavigationKey, reified A : Activity> activityDestination(): NavigationDestinationProvider<T> {
    return activityDestination(T::class, A::class)
}

public fun <T : NavigationKey, A : Activity> activityDestination(
    keyType: KClass<T>,
    activityType: KClass<A>,
): NavigationDestinationProvider<T> {
    return navigationDestination(
        metadata = {
            add(ActivityTypeKey to activityType)
            rootContextDestination()
        }
    ) {
        error("activityDestination should not be rendered directly. If you are reaching this, please report this as a bug.")
    }
}

internal const val ActivityTypeKey = "dev.enro.ui.destinations.ActivityDestinationKey"
private const val IntentInstanceKey = "dev.enro.ui.destinations.ActivityDestination.IntentInstanceKey"

public fun Intent.putNavigationKeyInstance(instance: NavigationKey.Instance<*>): Intent {
    return putExtra(
        IntentInstanceKey, Bundle().putNavigationKeyInstance(
            instance.copy(
                metadata = instance.metadata.copy()
            )
        )
    )
}

public fun Intent.getNavigationKeyInstance(): NavigationKey.Instance<NavigationKey>? {
    return getBundleExtra(IntentInstanceKey)?.getNavigationKeyInstance()
}
