package nav.enro.core

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlin.reflect.KClass

interface Navigator<T : NavigationKey> {
    val keyType: KClass<T>
    val defaultKey: NavigationKey?
    val contextType: KClass<*>
}

data class ActivityNavigator<T : NavigationKey>(
    override val keyType: KClass<T>,
    override val contextType: KClass<out FragmentActivity>,
    override val defaultKey: NavigationKey?
) : Navigator<T>

class FragmentNavigator<T : NavigationKey>(
    override val keyType: KClass<T>,
    override val contextType: KClass<out Fragment>,
    override val defaultKey: NavigationKey?
) : Navigator<T>

inline fun <reified T : NavigationKey, reified A : FragmentActivity> activityNavigator(defaultKey: NavigationKey? = null) =
    ActivityNavigator(
        keyType = T::class,
        contextType = A::class,
        defaultKey = defaultKey
    )

inline fun <reified T : NavigationKey, reified A : Fragment> fragmentNavigator(defaultKey: NavigationKey? = null) =
    FragmentNavigator(
        keyType = T::class,
        contextType = A::class,
        defaultKey = defaultKey
    )