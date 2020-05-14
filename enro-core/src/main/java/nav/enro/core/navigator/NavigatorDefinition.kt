package nav.enro.core.navigator

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import nav.enro.core.NavigationKey
import nav.enro.core.executors.NavigationExecutor
import kotlin.reflect.KClass

class NavigatorDefinition<OpensContext: Any, T: NavigationKey>(
    val navigator: Navigator<OpensContext, T>,
    val executors: List<NavigationExecutor<OpensContext, *, *>>
)

inline fun <reified T : NavigationKey, reified A : FragmentActivity> createActivityNavigator(
    block: ActivityNavigatorBuilder<T, A>.() -> Unit = {}
): NavigatorDefinition<A, T> =
    ActivityNavigatorBuilder(
        keyType = T::class,
        contextType = A::class
    ).apply(block).build()

class ActivityNavigatorBuilder<T: NavigationKey, C: FragmentActivity>(
    private val keyType: KClass<T>,
    @PublishedApi internal val contextType: KClass<C>
) {
    @PublishedApi internal val executors = mutableListOf<NavigationExecutor<C, *, *>>()
    private var defaultKey: NavigationKey? = null

    fun defaultKey(key: T) {
        defaultKey = key
    }

    fun build() = NavigatorDefinition(
        navigator = ActivityNavigator(
            keyType = keyType,
            contextType = contextType,
            defaultKey = defaultKey
        ),
        executors = executors
    )
}


inline fun <reified T : NavigationKey, reified A : Fragment> createFragmentNavigator(
    block: FragmentNavigatorBuilder<A, T>.() -> Unit = {}
): NavigatorDefinition<A, T> =
    FragmentNavigatorBuilder(
        keyType = T::class,
        contextType = A::class
    ).apply(block).build()

class FragmentNavigatorBuilder<C: Fragment, T: NavigationKey>(
    private val keyType: KClass<T>,
    private val contextType: KClass<C>
) {
    private val executors = mutableListOf<NavigationExecutor<C, *, *>>()

    fun build() = NavigatorDefinition(
        navigator = FragmentNavigator(
            keyType = keyType,
            contextType = contextType,
            defaultKey = null
        ),
        executors = executors
    )
}