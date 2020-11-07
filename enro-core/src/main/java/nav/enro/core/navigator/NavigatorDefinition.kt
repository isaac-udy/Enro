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

fun <T : NavigationKey, A : FragmentActivity> createActivityNavigator(
    navigationKeyType: KClass<T>,
    activityType: KClass<A>,
    block: ActivityNavigatorBuilder<T, A>.() -> Unit = {}
): NavigatorDefinition<A, T> =
    ActivityNavigatorBuilder(
        keyType = navigationKeyType,
        contextType = activityType
    ).apply(block).build()

inline fun <reified T : NavigationKey, reified A : FragmentActivity> createActivityNavigator(
    noinline block: ActivityNavigatorBuilder<T, A>.() -> Unit = {}
): NavigatorDefinition<A, T> = createActivityNavigator(T::class, A::class, block)

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


fun <T : NavigationKey, A : Fragment> createFragmentNavigator(
    navigationKeyType: KClass<T>,
    fragmentType: KClass<A>,
    block: FragmentNavigatorBuilder<A, T>.() -> Unit = {}
): NavigatorDefinition<A, T> =
    FragmentNavigatorBuilder(
        keyType = navigationKeyType,
        contextType = fragmentType,
    ).apply(block).build()

inline fun <reified T : NavigationKey, reified A : Fragment> createFragmentNavigator(
    noinline block: FragmentNavigatorBuilder<A, T>.() -> Unit = {}
): NavigatorDefinition<A, T> = createFragmentNavigator(T::class, A::class, block)

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


fun <T : NavigationKey> createSyntheticNavigator(
    navigationKeyType: KClass<T>,
    destination: SyntheticDestination<T>
): NavigatorDefinition<Any, T> =
    SyntheticNavigatorBuilder(
        keyType = navigationKeyType,
        destination = destination
    ).build()

inline fun <reified T : NavigationKey> createSyntheticNavigator(destination: SyntheticDestination<T>): NavigatorDefinition<Any, T> =
    createSyntheticNavigator(T::class, destination)

class SyntheticNavigatorBuilder<T: NavigationKey>(
    private val keyType: KClass<T>,
    private val destination: SyntheticDestination<T>
) {
    fun build() = NavigatorDefinition(
        navigator = SyntheticNavigator(
            keyType = keyType,
            destination = destination
        ),
        executors = emptyList()
    )
}