package nav.enro.core.navigator

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import nav.enro.core.NavigationKey
import nav.enro.core.NoNavigationKeyBound
import kotlin.reflect.KClass

interface Navigator<C: Any, T : NavigationKey> {
    val keyType: KClass<T>
    val contextType: KClass<C>
    val animations: NavigatorAnimations
}

class ActivityNavigator<C: FragmentActivity, T : NavigationKey> @PublishedApi internal constructor(
    override val keyType: KClass<T>,
    override val contextType: KClass<C>,
    override val animations: NavigatorAnimations = NavigatorAnimations.default
) : Navigator<C, T>

class FragmentNavigator<C: Fragment, T : NavigationKey> @PublishedApi internal constructor(
    override val keyType: KClass<T>,
    override val contextType: KClass<C>,
    override val animations: NavigatorAnimations = NavigatorAnimations.default
) : Navigator<C, T>

class SyntheticNavigator<T : NavigationKey> @PublishedApi internal constructor(
    override val keyType: KClass<T>,
    val destination: SyntheticDestination<T>
) : Navigator<Any, T> {
    override val contextType: KClass<Any> = Any::class
    override val animations: NavigatorAnimations = NavigatorAnimations.default
}

internal class NoKeyNavigator: Navigator<Any, NoNavigationKeyBound> {
    override val keyType: KClass<NoNavigationKeyBound> = NoNavigationKeyBound::class
    override val contextType: KClass<Any> = Any::class
    override val animations: NavigatorAnimations = NavigatorAnimations.default
}