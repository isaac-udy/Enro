@file:Suppress("PackageDirectoryMismatch") // Package name is intentionally different to avoid conflicts with the new API
package dev.enro.core.fragment

import androidx.fragment.app.Fragment
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.controller.NavigationModuleScope
import dev.enro.core.serialization.defaultSerializer
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

@Deprecated("Use dev.enro.destination.fragment.FragmentNavigationBinding")
public typealias FragmentNavigationBinding<KeyType, FragmentType> = dev.enro.destination.fragment.FragmentNavigationBinding<KeyType, FragmentType>

@Deprecated("Use dev.enro.destination.fragment.createFragmentNavigationBinding")
public fun <KeyType : NavigationKey, FragmentType : Fragment> createFragmentNavigationBinding(
    keyType: KClass<KeyType>,
    fragmentType: KClass<FragmentType>,
    serializer: KSerializer<KeyType>,
): NavigationBinding<KeyType, FragmentType> = dev.enro.destination.fragment.createFragmentNavigationBinding(
    keyType = keyType,
    fragmentType = fragmentType,
    keySerializer = serializer,
)

// Class-based overload for Java compatibility
@Deprecated("Use dev.enro.destination.fragment.createFragmentNavigationBinding")
public fun <KeyType : NavigationKey, FragmentType : Fragment> createFragmentNavigationBinding(
    keyType: Class<KeyType>,
    fragmentType: Class<FragmentType>,
    serializer: KSerializer<KeyType>,
): NavigationBinding<KeyType, FragmentType> = dev.enro.destination.fragment.createFragmentNavigationBinding(
    keyType = keyType.kotlin,
    fragmentType = fragmentType.kotlin,
    keySerializer = serializer,
)

@Deprecated("Use dev.enro.destination.fragment.createFragmentNavigationBinding")
public inline fun <reified KeyType : NavigationKey, reified FragmentType : Fragment> createFragmentNavigationBinding(): NavigationBinding<KeyType, FragmentType> =
    dev.enro.destination.fragment.createFragmentNavigationBinding(
        keyType = KeyType::class,
        fragmentType = FragmentType::class,
        keySerializer = NavigationKey.defaultSerializer(),
    )

@Deprecated("Use dev.enro.destination.fragment.fragmentDestination")
public inline fun <reified KeyType : NavigationKey, reified DestinationType : Fragment> NavigationModuleScope.fragmentDestination() {
    binding(dev.enro.destination.fragment.createFragmentNavigationBinding<KeyType, DestinationType>())
}

