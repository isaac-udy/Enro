package dev.enro.core.activity

import android.app.Activity
import androidx.activity.ComponentActivity
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.controller.NavigationComponentBuilder
import kotlin.reflect.KClass

public class ActivityNavigationBinding<KeyType : NavigationKey, ActivityType : ComponentActivity> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val destinationType: KClass<ActivityType>,
) : NavigationBinding<KeyType, ActivityType> {
    override val baseType: KClass<in ActivityType> = Activity::class
}

public fun <KeyType : NavigationKey, ActivityType : ComponentActivity> createActivityNavigationBinding(
    keyType: Class<KeyType>,
    activityType: Class<ActivityType>
): NavigationBinding<KeyType, ActivityType> = ActivityNavigationBinding(
    keyType = keyType.kotlin,
    destinationType = activityType.kotlin,
)

public inline fun <reified KeyType : NavigationKey, reified ActivityType : ComponentActivity> createActivityNavigationBinding(): NavigationBinding<KeyType, ActivityType> =
    createActivityNavigationBinding(
        keyType = KeyType::class.java,
        activityType = ActivityType::class.java,
    )

public inline fun <reified KeyType : NavigationKey, reified DestinationType : ComponentActivity> NavigationComponentBuilder.activityDestination() {
    binding(createActivityNavigationBinding<KeyType, DestinationType>())
}
