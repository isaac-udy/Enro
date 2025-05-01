package dev.enro.destination.uiviewcontroller

import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.controller.NavigationModuleScope
import kotlinx.serialization.KSerializer
import platform.UIKit.UIViewController
import kotlin.reflect.KClass

public class UIViewControllerNavigationBinding<KeyType : NavigationKey, WindowType : UIViewController> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val destinationType: KClass<WindowType>,
    override val keySerializer: KSerializer<KeyType>,
    internal val constructDestination: () -> WindowType,
) : NavigationBinding<KeyType, WindowType>() {
    override val baseType: KClass<in WindowType> = UIViewController::class
}

@PublishedApi
internal fun <KeyType : NavigationKey, DestinationType : UIViewController> createUIViewControllerNavigationBinding(
    keyType: KClass<KeyType>,
    destinationType: KClass<DestinationType>,
    keySerializer: KSerializer<KeyType>,
    constructDestination: () -> DestinationType,
): NavigationBinding<KeyType, DestinationType> {
    return UIViewControllerNavigationBinding(
        keyType = keyType,
        destinationType = destinationType,
        keySerializer = keySerializer,
        constructDestination = constructDestination,
    )
}

public inline fun <reified KeyType : NavigationKey, reified DestinationType : UIViewController> NavigationModuleScope.uiViewControllerDestination(
    keySerializer: KSerializer<KeyType>,
    noinline constructDestination: () -> DestinationType,
) {
    binding(
        createUIViewControllerNavigationBinding(
            keyType = KeyType::class,
            destinationType = DestinationType::class,
            keySerializer = keySerializer,
            constructDestination = constructDestination,
        )
    )
}