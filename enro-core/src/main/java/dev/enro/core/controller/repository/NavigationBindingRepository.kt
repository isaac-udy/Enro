package dev.enro.core.controller.repository

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.synthetic.SyntheticDestination
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
            // There's only one synthetic destination class that exists,
            // so we can't check for duplicates here or we'll crash if there's more
            // than one synthetic destination. It would be nice to fix this.
            if (it.destinationType != SyntheticDestination::class) {
                require(bindingsByDestinationType[it.destinationType] == it) {
                    "Found duplicated navigation binding! ${it.destinationType.java.name} has been bound to multiple navigation keys."
                }
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

internal fun NavigationBindingRepository.requireBindingForInstruction(
    instruction: AnyOpenInstruction
): NavigationBinding<*, *> {
    return bindingForKeyType(instruction.navigationKey::class)!!
}