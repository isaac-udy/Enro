package nav.enro.core.navigator

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import nav.enro.core.NavigationKey
import kotlin.reflect.KClass

interface Navigator<C: Any, T : NavigationKey> {
    val keyType: KClass<T>
    val defaultKey: NavigationKey?
    val contextType: KClass<C>
    val animations: NavigatorAnimations
}

data class ActivityNavigator<C: FragmentActivity, T : NavigationKey> @PublishedApi internal constructor(
    override val keyType: KClass<T>,
    override val contextType: KClass<C>,
    override val defaultKey: NavigationKey?,
    override val animations: NavigatorAnimations = NavigatorAnimations.defaultOverride
) : Navigator<C, T>

class FragmentNavigator<C: Fragment, T : NavigationKey> @PublishedApi internal constructor(
    override val keyType: KClass<T>,
    override val contextType: KClass<C>,
    override val defaultKey: NavigationKey?,
    override val animations: NavigatorAnimations = NavigatorAnimations.defaultOverride
) : Navigator<C, T>
