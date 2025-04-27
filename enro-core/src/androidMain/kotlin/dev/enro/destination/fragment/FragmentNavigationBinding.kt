package dev.enro.destination.fragment

import androidx.fragment.app.Fragment
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.NavigationKeySerializer
import dev.enro.core.controller.NavigationModuleScope
import dev.enro.core.default
import kotlin.reflect.KClass

public class FragmentNavigationBinding<KeyType : NavigationKey, FragmentType : Fragment> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val destinationType: KClass<FragmentType>,
    override val keySerializer: NavigationKeySerializer<KeyType> = NavigationKeySerializer.default(keyType),
) : NavigationBinding<KeyType, FragmentType>() {
    override val baseType: KClass<in FragmentType> = Fragment::class
}

public fun <KeyType : NavigationKey, FragmentType : Fragment> createFragmentNavigationBinding(
    keyType: KClass<KeyType>,
    fragmentType: KClass<FragmentType>
): NavigationBinding<KeyType, FragmentType> = FragmentNavigationBinding(
    keyType = keyType,
    destinationType = fragmentType,
)

// Class-based overload for Java compatibility
public fun <KeyType : NavigationKey, FragmentType : Fragment> createFragmentNavigationBinding(
    keyType: Class<KeyType>,
    fragmentType: Class<FragmentType>
): NavigationBinding<KeyType, FragmentType> = createFragmentNavigationBinding(
    keyType = keyType.kotlin,
    fragmentType = fragmentType.kotlin,
)

public inline fun <reified KeyType : NavigationKey, reified FragmentType : Fragment> createFragmentNavigationBinding(): NavigationBinding<KeyType, FragmentType> =
    createFragmentNavigationBinding(
        keyType = KeyType::class,
        fragmentType = FragmentType::class,
    )


public inline fun <reified KeyType : NavigationKey, reified DestinationType : Fragment> NavigationModuleScope.fragmentDestination() {
    binding(createFragmentNavigationBinding<KeyType, DestinationType>())
}

