package dev.enro.destination.compose

import androidx.compose.runtime.Composable
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey


// Class-based overload for Java compatibility
public fun <KeyType : NavigationKey> createComposableNavigationBinding(
    keyType: Class<KeyType>,
    content: @Composable () -> Unit
): NavigationBinding<KeyType, ComposableDestination> {
    return createComposableNavigationBinding(
        keyType = keyType.kotlin,
        content = content
    )
}

// Class-based overload for Java compatibility
public fun <KeyType : NavigationKey, ComposableType : ComposableDestination> createComposableNavigationBinding(
    keyType: Class<KeyType>,
    composableType: Class<ComposableType>
): NavigationBinding<KeyType, ComposableType> {
    return createComposableNavigationBinding(
        keyType = keyType.kotlin,
        destinationType = composableType.kotlin
    )
}
