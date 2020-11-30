package nav.enro.core.activity

import androidx.fragment.app.FragmentActivity
import nav.enro.core.NavigationKey
import nav.enro.core.Navigator
import nav.enro.core.NavigatorAnimations
import kotlin.reflect.KClass

class ActivityNavigator<KeyType : NavigationKey, ActivityType : FragmentActivity> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val contextType: KClass<ActivityType>,
    override val animations: NavigatorAnimations = NavigatorAnimations.default
) : Navigator<KeyType, ActivityType>

fun <KeyType : NavigationKey, ActivityType : FragmentActivity> createActivityNavigator(
    keyType: Class<KeyType>,
    activityType: Class<ActivityType>
): Navigator<KeyType, ActivityType> = ActivityNavigator(
    keyType = keyType.kotlin,
    contextType = activityType.kotlin,
)

inline fun <reified KeyType : NavigationKey, reified ActivityType : FragmentActivity> createActivityNavigator(): Navigator<KeyType, ActivityType> =
    createActivityNavigator(
        keyType = KeyType::class.java,
        activityType = ActivityType::class.java,
    )