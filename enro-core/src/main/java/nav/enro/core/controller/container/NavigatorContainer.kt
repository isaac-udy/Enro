package nav.enro.core.controller.container

import nav.enro.core.NavigationKey
import nav.enro.core.Navigator
import nav.enro.core.activity.createActivityNavigator
import nav.enro.core.controller.NavigationController
import nav.enro.core.fragment.internal.HiltSingleFragmentActivity
import nav.enro.core.fragment.internal.SingleFragmentActivity
import nav.enro.core.fragment.internal.SingleFragmentKey
import nav.enro.core.internal.NoKeyNavigator
import nav.enro.core.plugins.EnroHilt
import kotlin.reflect.KClass

internal class NavigatorContainer (
    private val navigators: List<Navigator<*, *>>,
    private val useHiltDefaults: Boolean,
) {
    private val defaultNavigators = run {
        val singleFragmentNavigator = if(useHiltDefaults) {
            createActivityNavigator<SingleFragmentKey, HiltSingleFragmentActivity>()
        }
        else {
            createActivityNavigator<SingleFragmentKey, SingleFragmentActivity>()
        }

        val noKeyProvidedNavigator = NoKeyNavigator()

        listOf(
            singleFragmentNavigator,
            noKeyProvidedNavigator
        )
    }

    private val navigatorsByKeyType = (navigators + defaultNavigators)
        .map {
            it.keyType to it
        }
        .toMap()

    private val navigatorsByContextType = (navigators + defaultNavigators)
        .map {
            it.contextType to it
        }
        .toMap()

    fun navigatorForContextType(
        contextType: KClass<*>
    ): Navigator<*, *>? {
        return navigatorsByContextType[contextType]
    }

    fun navigatorForKeyType(
        keyType: KClass<out NavigationKey>
    ): Navigator<*, *>? {
        return navigatorsByKeyType[keyType]
    }
}