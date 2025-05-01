package dev.enro.destination.compose

import androidx.compose.runtime.Composable
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import kotlinx.serialization.KSerializer

// Class-based overload for Java compatibility
public fun <KeyType : NavigationKey, ComposableType : ComposableDestination> createComposableNavigationBinding(
    keyType: Class<KeyType>,
    keySerializer: KSerializer<KeyType>,
    composableType: Class<ComposableType>,
    constructDestination: () -> ComposableType,
): NavigationBinding<KeyType, ComposableType> {
    return createComposableNavigationBinding(
        keyType = keyType.kotlin,
        keySerializer = keySerializer,
        destinationType = composableType.kotlin,
        constructDestination = constructDestination,
    )
}
