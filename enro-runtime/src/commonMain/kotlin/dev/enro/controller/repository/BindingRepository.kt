package dev.enro.controller.repository

import dev.enro.NavigationBinding
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination
import kotlin.reflect.KClass

internal class BindingRepository(
    private val plugins: PluginRepository,
) {
    private val bindingsByKeyType = mutableMapOf<KClass<*>, NavigationBinding<*>>()
    private val originalBindingsByKeyType = mutableMapOf<KClass<*>, NavigationBinding<*>>()

    fun addNavigationBindings(binding: List<NavigationBinding<*>>) {
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
                    return@forEach
                }

                isValidBinding -> {
                    // If the existing binding is not a platform override, and the new binding is,
                    // then we should replace the existing binding.
                    bindingsByKeyType[it.keyType] = it
                    if (existingBinding != null) {
                        originalBindingsByKeyType[existingBinding.keyType] = existingBinding
                    }
                }

                else -> {
                    error("An unknown error occurred while adding the binding for ${it.keyType.qualifiedName}.")
                }
            }
        }
    }

    fun <K : NavigationKey> bindingFor(
        instance: NavigationKey.Instance<K>,
    ): NavigationBinding<K> {
        val binding = when {
            NavigationBinding.usesOriginalBinding(instance) ->
                originalBindingsByKeyType[instance.key::class]
                    ?: bindingsByKeyType[instance.key::class]

            else -> bindingsByKeyType[instance.key::class]
        }
        @Suppress("UNCHECKED_CAST")
        return requireNotNull(binding) {
            "No binding found for ${instance.key::class.qualifiedName}"
        } as NavigationBinding<K>
    }

    fun <K : NavigationKey> destinationFor(
        instance: NavigationKey.Instance<K>,
    ): NavigationDestination<K> {
        // Create the destination, and allow plugins to intercept and add
        val binding = bindingFor(instance)
        val destination = binding.provider.create(instance)

        val additionalMetadata = mutableMapOf<String, Any?>()
        plugins.onDestinationCreated(
            destination = destination,
            additionalMetadata = additionalMetadata,
        )
        if (additionalMetadata.isEmpty()) return destination

        val updatedMetadata = (destination.metadata + additionalMetadata)
            .mapNotNull { (key, value) ->
                when (value) {
                    null -> null
                    else -> key to value
                }
            }
            .toMap()

        return destination.copy(
            metadata = updatedMetadata,
        )
    }
}