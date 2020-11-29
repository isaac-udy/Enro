package nav.enro.core.navigator

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import nav.enro.core.NavigationKey
import kotlin.reflect.KClass

fun <T : NavigationKey, A : FragmentActivity> createActivityNavigator(
    navigationKeyType: KClass<T>,
    activityType: KClass<A>,
    block: ActivityNavigatorBuilder<T, A>.() -> Unit = {}
): Navigator<A, T> =
    ActivityNavigatorBuilder(
        keyType = navigationKeyType,
        contextType = activityType
    ).apply(block).build()

inline fun <reified T : NavigationKey, reified A : FragmentActivity> createActivityNavigator(
    noinline block: ActivityNavigatorBuilder<T, A>.() -> Unit = {}
): Navigator<A, T> = createActivityNavigator(T::class, A::class, block)

class ActivityNavigatorBuilder<T : NavigationKey, C : FragmentActivity>(
    private val keyType: KClass<T>,
    @PublishedApi internal val contextType: KClass<C>
) {
    fun build() = ActivityNavigator(
        keyType = keyType,
        contextType = contextType,
    )
}

fun <T : NavigationKey, A : Fragment> createFragmentNavigator(
    navigationKeyType: KClass<T>,
    fragmentType: KClass<A>,
    block: FragmentNavigatorBuilder<A, T>.() -> Unit = {}
): Navigator<A, T> =
    FragmentNavigatorBuilder(
        keyType = navigationKeyType,
        contextType = fragmentType,
    ).apply(block).build()

inline fun <reified T : NavigationKey, reified A : Fragment> createFragmentNavigator(
    noinline block: FragmentNavigatorBuilder<A, T>.() -> Unit = {}
): Navigator<A, T> = createFragmentNavigator(T::class, A::class, block)

class FragmentNavigatorBuilder<C : Fragment, T : NavigationKey>(
    private val keyType: KClass<T>,
    private val contextType: KClass<C>
) {
    fun build() = FragmentNavigator(
        keyType = keyType,
        contextType = contextType,
    )
}

fun <T : NavigationKey> createSyntheticNavigator(
    navigationKeyType: KClass<T>,
    destination: SyntheticDestination<T>
): Navigator<Any, T> =
    SyntheticNavigatorBuilder(
        keyType = navigationKeyType,
        destination = destination
    ).build()

inline fun <reified T : NavigationKey> createSyntheticNavigator(destination: SyntheticDestination<T>): Navigator<Any, T> =
    createSyntheticNavigator(T::class, destination)

class SyntheticNavigatorBuilder<T : NavigationKey>(
    private val keyType: KClass<T>,
    private val destination: SyntheticDestination<T>
) {
    fun build() = SyntheticNavigator(
        keyType = keyType,
        destination = destination
    )
}