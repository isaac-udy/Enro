package nav.enro.core.synthetic

import nav.enro.core.NavigationKey
import nav.enro.core.Navigator
import kotlin.reflect.KClass


class SyntheticNavigator<KeyType : NavigationKey> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    val destination: SyntheticDestination<KeyType>
) : Navigator<KeyType, SyntheticDestination<*>> {
    override val contextType: KClass<SyntheticDestination<*>> = SyntheticDestination::class
}

fun <T : NavigationKey> createSyntheticNavigator(
    navigationKeyType: Class<T>,
    destination: SyntheticDestination<T>
): Navigator<T, SyntheticDestination<*>> =
    SyntheticNavigator(
        keyType = navigationKeyType.kotlin,
        destination = destination
    )

inline fun <reified KeyType : NavigationKey> createSyntheticNavigator(
    destination: SyntheticDestination<KeyType>
): Navigator<KeyType, SyntheticDestination<*>> =
    SyntheticNavigator(
        keyType = KeyType::class,
        destination = destination
    )