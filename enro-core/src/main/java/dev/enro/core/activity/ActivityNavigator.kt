package dev.enro.core.activity

import androidx.activity.ComponentActivity
import dev.enro.core.NavigationKey
import dev.enro.core.Navigator
import kotlin.reflect.KClass

class ActivityNavigator<KeyType : NavigationKey, ActivityType : ComponentActivity> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val contextType: KClass<ActivityType>,
) : Navigator<KeyType, ActivityType>

fun <KeyType : NavigationKey, ActivityType : ComponentActivity> createActivityNavigator(
    keyType: Class<KeyType>,
    activityType: Class<ActivityType>
): Navigator<KeyType, ActivityType> = ActivityNavigator(
    keyType = keyType.kotlin,
    contextType = activityType.kotlin,
)

inline fun <reified KeyType : NavigationKey, reified ActivityType : ComponentActivity> createActivityNavigator(): Navigator<KeyType, ActivityType> =
    createActivityNavigator(
        keyType = KeyType::class.java,
        activityType = ActivityType::class.java,
    )