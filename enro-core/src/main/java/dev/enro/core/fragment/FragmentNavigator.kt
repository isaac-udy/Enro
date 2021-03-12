package dev.enro.core.fragment

import androidx.fragment.app.Fragment
import nav.enro.core.NavigationKey
import nav.enro.core.Navigator
import kotlin.reflect.KClass

class FragmentNavigator<KeyType : NavigationKey, FragmentType: Fragment> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val contextType: KClass<FragmentType>,
) : Navigator<KeyType, FragmentType>

fun <KeyType : NavigationKey, FragmentType : Fragment> createFragmentNavigator(
    keyType: Class<KeyType>,
    fragmentType: Class<FragmentType>
): Navigator<KeyType, FragmentType> = FragmentNavigator(
    keyType = keyType.kotlin,
    contextType = fragmentType.kotlin,
)

inline fun <reified KeyType : NavigationKey, reified FragmentType : Fragment> createFragmentNavigator(): Navigator<KeyType, FragmentType> =
    createFragmentNavigator(
        keyType = KeyType::class.java,
        fragmentType = FragmentType::class.java,
    )