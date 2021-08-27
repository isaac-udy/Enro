package dev.enro.core.controller.container

import dev.enro.core.NavigationKey
import dev.enro.core.Navigator
import dev.enro.core.activity.createActivityNavigator
import dev.enro.core.compose.ComposeFragmentHost
import dev.enro.core.compose.ComposeFragmentHostKey
import dev.enro.core.compose.HiltComposeFragmentHost
import dev.enro.core.fragment.createFragmentNavigator
import dev.enro.core.fragment.internal.HiltSingleFragmentActivity
import dev.enro.core.fragment.internal.SingleFragmentActivity
import dev.enro.core.fragment.internal.SingleFragmentKey
import dev.enro.core.internal.NoKeyNavigator
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

        val hostedComposeNavigator = if(useHiltDefaults) {
            createFragmentNavigator<ComposeFragmentHostKey, HiltComposeFragmentHost>()
        }
        else {
            createFragmentNavigator<ComposeFragmentHostKey, ComposeFragmentHost>()
        }

        listOf(
            singleFragmentNavigator,
            noKeyProvidedNavigator,
            hostedComposeNavigator
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

    init {
        navigators.forEach {
            require(navigatorsByKeyType[it.keyType] == it) {
                "Found duplicated navigator binding! ${it.keyType.java.name} has been bound to multiple destinations."
            }
        }
    }

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