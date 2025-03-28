package dev.enro.core.activity

import android.app.Activity
import androidx.activity.ComponentActivity
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.NavigationKeySerializer
import dev.enro.core.controller.NavigationModuleScope
import dev.enro.core.default
import kotlin.reflect.KClass

public class ActivityNavigationBinding<KeyType : NavigationKey, ActivityType : ComponentActivity> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val destinationType: KClass<ActivityType>,
    override val keySerializer: NavigationKeySerializer<KeyType> = NavigationKeySerializer.default(keyType),
) : NavigationBinding<KeyType, ActivityType> {
    override val baseType: KClass<in ActivityType> = Activity::class
}

public fun <KeyType : NavigationKey, ActivityType : ComponentActivity> createActivityNavigationBinding(
    keyType: KClass<KeyType>,
    activityType: KClass<ActivityType>
): NavigationBinding<KeyType, ActivityType> {
    return ActivityNavigationBinding(
        keyType = keyType,
        destinationType = activityType,
    )
}

// Class-based overload for Java compatibility
public fun <KeyType : NavigationKey, ActivityType : ComponentActivity> createActivityNavigationBinding(
    keyType: Class<KeyType>,
    activityType: Class<ActivityType>
): NavigationBinding<KeyType, ActivityType> {
    return createActivityNavigationBinding(
        keyType = keyType.kotlin,
        activityType = activityType.kotlin,
    )
}

public inline fun <reified KeyType : NavigationKey, reified ActivityType : ComponentActivity> createActivityNavigationBinding(): NavigationBinding<KeyType, ActivityType> {
    return createActivityNavigationBinding(
        keyType = KeyType::class,
        activityType = ActivityType::class,
    )
}

public inline fun <reified KeyType : NavigationKey, reified DestinationType : ComponentActivity> NavigationModuleScope.activityDestination() {
    binding(createActivityNavigationBinding<KeyType, DestinationType>())
}
