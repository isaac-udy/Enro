package dev.enro.destination.desktop

import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.NavigationKeySerializer
import dev.enro.core.controller.NavigationModuleScope
import dev.enro.core.default
import kotlin.reflect.KClass

public class DesktopWindowNavigationBinding<KeyType : NavigationKey, WindowType : DesktopWindow> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val destinationType: KClass<WindowType>,
    internal val constructDestination: () -> WindowType,
    override val keySerializer: NavigationKeySerializer<KeyType> = NavigationKeySerializer.default(keyType),
) : NavigationBinding<KeyType, WindowType> {
    override val baseType: KClass<in WindowType> = DesktopWindow::class
}

@PublishedApi
internal fun <KeyType : NavigationKey, DestinationType : DesktopWindow> createDesktopWindowNavigationBinding(
    keyType: KClass<KeyType>,
    destinationType: KClass<DestinationType>,
    constructDestination: () -> DestinationType,
): NavigationBinding<KeyType, DestinationType> {
    return DesktopWindowNavigationBinding(
        keyType = keyType,
        destinationType = destinationType,
        constructDestination = constructDestination,
    )
}

public inline fun <reified KeyType : NavigationKey, reified DestinationType : DesktopWindow> NavigationModuleScope.desktopWindowDestination(
    noinline constructDestination: () -> DestinationType,
) {
    binding(
        createDesktopWindowNavigationBinding(
            keyType = KeyType::class,
            destinationType = DestinationType::class,
            constructDestination = constructDestination,
        )
    )
}
