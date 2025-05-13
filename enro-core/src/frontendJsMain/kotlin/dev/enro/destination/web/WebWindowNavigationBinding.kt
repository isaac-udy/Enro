package dev.enro.destination.web

import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.controller.NavigationModuleScope
import dev.enro.core.serialization.defaultSerializer
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

public class WebWindowNavigationBinding<KeyType : NavigationKey, WindowType : WebWindow> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val destinationType: KClass<WindowType>,
    internal val constructDestination: () -> WindowType,
    override val keySerializer: KSerializer<KeyType>,
) : NavigationBinding<KeyType, WindowType>() {
    override val baseType: KClass<in WindowType> = WebWindow::class
}

@PublishedApi
internal fun <KeyType : NavigationKey, DestinationType : WebWindow> createWebWindowNavigationBinding(
    keyType: KClass<KeyType>,
    destinationType: KClass<DestinationType>,
    keySerializer: KSerializer<KeyType>,
    constructDestination: () -> DestinationType,
): NavigationBinding<KeyType, DestinationType> {
    return WebWindowNavigationBinding(
        keyType = keyType,
        destinationType = destinationType,
        keySerializer = keySerializer,
        constructDestination = constructDestination,
    )
}

public inline fun <reified KeyType : NavigationKey, reified DestinationType : WebWindow> NavigationModuleScope.webWindowDestination(
    keySerializer: KSerializer<KeyType> = NavigationKey.defaultSerializer(),
    noinline constructDestination: () -> DestinationType,
) {
    binding(
        createWebWindowNavigationBinding(
            keyType = KeyType::class,
            destinationType = DestinationType::class,
            keySerializer = keySerializer,
            constructDestination = constructDestination,
        )
    )
}
