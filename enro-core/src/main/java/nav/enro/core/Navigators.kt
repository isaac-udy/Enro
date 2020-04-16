package nav.enro.core

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import nav.enro.core.internal.context.FragmentHost
import nav.enro.core.internal.context.FragmentHostDefinition
import kotlin.reflect.KClass

interface Navigator<T : NavigationKey> {
    val keyType: KClass<T>
    val defaultKey: NavigationKey?
    val contextType: KClass<*>
    val fragmentHosts: List<FragmentHostDefinition>
}

data class ActivityNavigator<T : NavigationKey>(
    override val keyType: KClass<T>,
    override val contextType: KClass<out FragmentActivity>,
    override val defaultKey: NavigationKey?,
    override val fragmentHosts: List<FragmentHostDefinition>
) : Navigator<T>

class FragmentNavigator<T : NavigationKey>(
    override val keyType: KClass<T>,
    override val contextType: KClass<out Fragment>,
    override val defaultKey: NavigationKey?,
    override val fragmentHosts: List<FragmentHostDefinition>
) : Navigator<T>

inline fun <reified T : NavigationKey, reified A : FragmentActivity> activityNavigator(block: ActivityNavigatorBuilder<T>.() -> Unit = {}) =
    ActivityNavigatorBuilder(
        keyType = T::class,
        contextType = A::class
    ).apply(block).build()

inline fun <reified T : NavigationKey, reified A : Fragment> fragmentNavigator(block: FragmentNavigatorBuilder<T>.() -> Unit = {}) =
    FragmentNavigatorBuilder(
        keyType = T::class,
        contextType = A::class
    ).apply(block).build()

class ActivityNavigatorBuilder<T: NavigationKey>(
    private val keyType: KClass<T>,
    private val contextType: KClass<out FragmentActivity>
) {
    private val fragmentHosts = mutableListOf<FragmentHostDefinition>()
    private var defaultKey: T? = null

    fun defaultKey(key: T) {
        defaultKey = key
    }

    fun fragmentHost(containerView: Int, accept: (navigationKey: NavigationKey) -> Boolean) {
        fragmentHosts.add (
            FragmentHostDefinition(containerView, accept)
        )
    }

    fun build() = ActivityNavigator(
        keyType = keyType,
        contextType = contextType,
        defaultKey = defaultKey,
        fragmentHosts = fragmentHosts
    )
}

class FragmentNavigatorBuilder<T: NavigationKey>(
    private val keyType: KClass<T>,
    private val contextType: KClass<out Fragment>
) {
    private val fragmentHosts = mutableListOf<FragmentHostDefinition>()
    private var defaultKey: NavigationKey? = null

    fun fragmentHost(containerView: Int, accept: (navigationKey: NavigationKey) -> Boolean) {
        fragmentHosts.add (
            FragmentHostDefinition(containerView, accept)
        )
    }

    fun build() = FragmentNavigator(
        keyType = keyType,
        contextType = contextType,
        defaultKey = defaultKey,
        fragmentHosts = fragmentHosts
    )
}