package dev.enro.core.controller.repository

import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import kotlin.reflect.KClass

internal class NavigationBindingRepository {
    private val bindingsByKeyType = mutableMapOf<KClass<*>, NavigationBinding<*, *>>()
    private val bindingsByDestinationType = mutableMapOf<KClass<*>, NavigationBinding<*, *>>()

    fun addNavigationBindings(binding: List<NavigationBinding<*, *>>) {
        bindingsByKeyType += binding.associateBy { it.keyType }
        bindingsByDestinationType += binding.associateBy { it.destinationType }

        binding.forEach {
            require(bindingsByKeyType[it.keyType] == it) {
                "Found duplicated navigation binding! ${it.keyType.java.name} has been bound to multiple destinations."
            }
            require(bindingsByDestinationType[it.destinationType] == it) {
                "Found duplicated navigation binding! ${it.destinationType.java.name} has been bound to multiple navigation keys."
            }
        }
    }

    fun bindingForDestinationType(
        contextType: KClass<*>
    ): NavigationBinding<*, *>? {
        return bindingsByDestinationType[contextType]
    }

    fun bindingForKeyType(
        keyType: KClass<out NavigationKey>
    ): NavigationBinding<*, *>? {
        return bindingsByKeyType[keyType]
    }
}