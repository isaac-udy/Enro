package dev.enro.destination.web

import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
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