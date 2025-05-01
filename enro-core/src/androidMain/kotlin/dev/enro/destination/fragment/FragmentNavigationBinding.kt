package dev.enro.destination.fragment

import androidx.fragment.app.Fragment
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.controller.NavigationModuleScope
import dev.enro.core.defaultSerializer
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

public class FragmentNavigationBinding<KeyType : NavigationKey, FragmentType : Fragment> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val destinationType: KClass<FragmentType>,
    override val keySerializer: KSerializer<KeyType>,
) : NavigationBinding<KeyType, FragmentType>() {
    override val baseType: KClass<in FragmentType> = Fragment::class
}

public fun <KeyType : NavigationKey, FragmentType : Fragment> createFragmentNavigationBinding(
    keyType: KClass<KeyType>,
    keySerializer: KSerializer<KeyType>,
    fragmentType: KClass<FragmentType>,
): NavigationBinding<KeyType, FragmentType> = FragmentNavigationBinding(
    keyType = keyType,
    destinationType = fragmentType,
    keySerializer = keySerializer,
)

// Class-based overload for Java compatibility
public fun <KeyType : NavigationKey, FragmentType : Fragment> createFragmentNavigationBinding(
    keyType: Class<KeyType>,
    keySerializer: KSerializer<KeyType>,
    fragmentType: Class<FragmentType>,
): NavigationBinding<KeyType, FragmentType> = createFragmentNavigationBinding(
    keyType = keyType.kotlin,
    fragmentType = fragmentType.kotlin,
    keySerializer = keySerializer,
)

public inline fun <reified KeyType : NavigationKey, reified FragmentType : Fragment> createFragmentNavigationBinding(
    keySerializer: KSerializer<KeyType> = NavigationKey.defaultSerializer(),
): NavigationBinding<KeyType, FragmentType> =
    createFragmentNavigationBinding(
        keyType = KeyType::class,
        keySerializer = keySerializer,
        fragmentType = FragmentType::class,
    )


public inline fun <reified KeyType : NavigationKey, reified DestinationType : Fragment> NavigationModuleScope.fragmentDestination(
    keySerializer: KSerializer<KeyType> = NavigationKey.defaultSerializer(),
) {
    binding(createFragmentNavigationBinding<KeyType, DestinationType>())
}

