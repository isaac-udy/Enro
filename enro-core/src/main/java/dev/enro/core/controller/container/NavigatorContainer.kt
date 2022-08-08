package dev.enro.core.controller.container

import dev.enro.core.NavigationKey
import dev.enro.core.Navigator
import kotlin.reflect.KClass

internal class NavigatorContainer {
    private val navigatorsByKeyType = mutableMapOf<KClass<*>, Navigator<*, *>>()
    private val navigatorsByContextType = mutableMapOf<KClass<*>, Navigator<*, *>>()

    fun addNavigators(navigators: List<Navigator<*, *>>) {
        navigatorsByKeyType += navigators.associateBy { it.keyType }
        navigatorsByContextType += navigators.associateBy { it.contextType }

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