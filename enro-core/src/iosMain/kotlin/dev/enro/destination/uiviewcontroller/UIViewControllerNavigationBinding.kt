package dev.enro.destination.uiviewcontroller

import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.NavigationKeySerializer
import dev.enro.core.controller.NavigationModuleScope
import dev.enro.core.default
import platform.UIKit.UIViewController
import kotlin.reflect.KClass

public class UIViewControllerNavigationBinding<KeyType : NavigationKey, WindowType : UIViewController> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val destinationType: KClass<WindowType>,
    internal val constructDestination: () -> WindowType,
    override val keySerializer: NavigationKeySerializer<KeyType> = NavigationKeySerializer.default(keyType),
) : NavigationBinding<KeyType, WindowType>() {
    override val baseType: KClass<in WindowType> = UIViewController::class
}

@PublishedApi
internal fun <KeyType : NavigationKey, DestinationType : UIViewController> createUIViewControllerNavigationBinding(
    keyType: KClass<KeyType>,
    destinationType: KClass<DestinationType>,
    constructDestination: () -> DestinationType,
): NavigationBinding<KeyType, DestinationType> {
    return UIViewControllerNavigationBinding(
        keyType = keyType,
        destinationType = destinationType,
        constructDestination = constructDestination,
    )
}

public inline fun <reified KeyType : NavigationKey, reified DestinationType : UIViewController> NavigationModuleScope.uiViewControllerDestination(
    noinline constructDestination: () -> DestinationType,
) {
    binding(
        createUIViewControllerNavigationBinding(
            keyType = KeyType::class,
            destinationType = DestinationType::class,
            constructDestination = constructDestination,
        )
    )
}
