package dev.enro.core.controller.container

import dev.enro.core.NavigationKey
import dev.enro.core.Navigator
import dev.enro.core.activity.createActivityNavigator
import dev.enro.core.compose.*
import dev.enro.core.compose.ComposeFragmentHostKey
import dev.enro.core.compose.dialog.HiltComposeDialogFragmentHostKey
import dev.enro.core.compose.HiltComposeFragmentHostKey
import dev.enro.core.compose.dialog.ComposeDialogFragmentHost
import dev.enro.core.compose.dialog.ComposeDialogFragmentHostKey
import dev.enro.core.compose.dialog.HiltComposeDialogFragmentHost
import dev.enro.core.fragment.createFragmentNavigator
import dev.enro.core.fragment.internal.HiltSingleFragmentActivity
import dev.enro.core.fragment.internal.HiltSingleFragmentKey
import dev.enro.core.fragment.internal.SingleFragmentActivity
import dev.enro.core.fragment.internal.SingleFragmentKey
import dev.enro.core.internal.NoKeyNavigator
import kotlin.reflect.KClass

internal class NavigatorContainer(
    private val navigators: List<Navigator<*, *>>,
) {
    private val defaultNavigators = run {
        val singleFragmentNavigator =
            createActivityNavigator<SingleFragmentKey, SingleFragmentActivity>()

        val hiltSingleFragmentNavigator = runCatching {
            createActivityNavigator<HiltSingleFragmentKey, HiltSingleFragmentActivity>()
        }.getOrNull()

        val noKeyProvidedNavigator = NoKeyNavigator()

        val composeFragmentHostKey =
            createFragmentNavigator<ComposeFragmentHostKey, ComposeFragmentHost>()

        val hiltComposeFragmentHostKey = runCatching {
            createFragmentNavigator<HiltComposeFragmentHostKey, HiltComposeFragmentHost>()
        }.getOrNull()

        val composeDialogFragmentHostKey =
            createFragmentNavigator<ComposeDialogFragmentHostKey, ComposeDialogFragmentHost>()

        val hiltComposeDialogFragmentHostKey = runCatching {
            createFragmentNavigator<HiltComposeDialogFragmentHostKey, HiltComposeDialogFragmentHost>()
        }.getOrNull()

        listOfNotNull(
            singleFragmentNavigator,
            hiltSingleFragmentNavigator,
            noKeyProvidedNavigator,
            composeFragmentHostKey,
            hiltComposeFragmentHostKey,
            composeDialogFragmentHostKey,
            hiltComposeDialogFragmentHostKey,
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