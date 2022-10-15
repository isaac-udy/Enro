package dev.enro.core.fragment

import androidx.fragment.app.Fragment
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import kotlin.reflect.KClass

class FragmentNavigationBinding<KeyType : NavigationKey, FragmentType : Fragment> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val destinationType: KClass<FragmentType>,
) : NavigationBinding<KeyType, FragmentType>

fun <KeyType : NavigationKey, FragmentType : Fragment> createFragmentNavigationBinding(
    keyType: Class<KeyType>,
    fragmentType: Class<FragmentType>
): NavigationBinding<KeyType, FragmentType> = FragmentNavigationBinding(
    keyType = keyType.kotlin,
    destinationType = fragmentType.kotlin,
)

inline fun <reified KeyType : NavigationKey, reified FragmentType : Fragment> createFragmentNavigationBinding(): NavigationBinding<KeyType, FragmentType> =
    createFragmentNavigationBinding(
        keyType = KeyType::class.java,
        fragmentType = FragmentType::class.java,
    )