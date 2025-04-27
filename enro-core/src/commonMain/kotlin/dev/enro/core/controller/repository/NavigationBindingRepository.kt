package dev.enro.core.controller.repository

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationBinding
import dev.enro.core.shouldUseOriginalBinding
import kotlin.reflect.KClass

internal class NavigationBindingRepository {
    private val bindingsByKeyType = mutableMapOf<KClass<*>, NavigationBinding<*, *>>()
    private val bindingsByDestinationType = mutableMapOf<KClass<*>, NavigationBinding<*, *>>()

    private val originalBindingsByKeyType = mutableMapOf<KClass<*>, NavigationBinding<*, *>>()
    private val originalBindingsByDestinationType =
        mutableMapOf<KClass<*>, NavigationBinding<*, *>>()

    fun addNavigationBindings(binding: List<NavigationBinding<*, *>>) {
        binding.forEach { it ->
            val existingBinding = bindingsByKeyType[it.keyType]
            val existingIsPlatformOverride = existingBinding?.isPlatformOverride == true

            val multiplePlatformOverrides = existingIsPlatformOverride
                    && it.isPlatformOverride

            val multipleRegularBindings = existingBinding != null
                    && !existingIsPlatformOverride
                    && !it.isPlatformOverride

            val platformOverrideAlreadyBound = existingIsPlatformOverride
                    && !it.isPlatformOverride

            val isValidBinding = existingBinding == null
                    || existingBinding == it
                    || (it.isPlatformOverride && !existingBinding.isPlatformOverride)

            when {
                multiplePlatformOverrides -> error(
                    "Found multiple platform override bindings for ${it.keyType.qualifiedName}." +
                            " Please ensure that only one binding is provided for each key type."
                )

                multipleRegularBindings -> error(
                    "Found multiple bindings for ${it.keyType.qualifiedName}." +
                            " Please ensure that only one binding is provided for each key type, or use @PlatformOverride to override an existing binding for a specific platform."
                )

                platformOverrideAlreadyBound -> {
                    // If an existing binding is a platform override, and the new binding is not,
                    // then we should not replace the existing binding.
                    originalBindingsByKeyType[it.keyType] = it
                    originalBindingsByDestinationType[it.destinationType] = it
                    return@forEach
                }

                isValidBinding -> {
                    // If the existing binding is not a platform override, and the new binding is,
                    // then we should replace the existing binding.
                    bindingsByKeyType[it.keyType] = it
                    bindingsByDestinationType[it.destinationType] = it
                    if (existingBinding != null) {
                        originalBindingsByKeyType[existingBinding.keyType] = existingBinding
                        originalBindingsByDestinationType[existingBinding.destinationType] =
                            existingBinding
                    }
                }

                else -> {
                    error("An unknown error occurred while adding the binding for ${it.keyType.qualifiedName}.")
                }
            }
        }
    }

    fun bindingForInstruction(
        instruction: AnyOpenInstruction,
    ): NavigationBinding<*, *>? {
        return when {
            instruction.extras.shouldUseOriginalBinding() ->
                originalBindingsByKeyType[instruction.navigationKey::class]
                    ?: bindingsByKeyType[instruction.navigationKey::class]

            else -> bindingsByKeyType[instruction.navigationKey::class]
        }
    }
}