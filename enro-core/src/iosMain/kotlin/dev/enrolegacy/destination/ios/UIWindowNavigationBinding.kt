package dev.enrolegacy.destination.ios

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.NavigationModuleScope
import kotlinx.serialization.KSerializer
import platform.UIKit.UIWindow
import kotlin.reflect.KClass

public class UIWindowNavigationBinding<KeyType : NavigationKey, WindowType : UIWindow> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val destinationType: KClass<WindowType>,
    override val keySerializer: KSerializer<KeyType>,
    internal val windowProvider: UIWindowProvider<KeyType>,
) : NavigationBinding<KeyType, WindowType>() {
    override val baseType: KClass<in WindowType> = UIWindow::class
}

public class UIWindowScope<KeyType: NavigationKey>(
    public val navigationKey: KeyType,
    public val controller: NavigationController,
    public val instruction: AnyOpenInstruction,
)

public interface UIWindowProvider<KeyType: NavigationKey> {
    public fun createUIWindow(scope: UIWindowScope<KeyType>): UIWindow
}

public fun <KeyType: NavigationKey> uiWindowDestination(
    block: UIWindowScope<KeyType>.() -> UIWindow,
): UIWindowProvider<KeyType> {
    return object : UIWindowProvider<KeyType> {
        override fun createUIWindow(scope: UIWindowScope<KeyType>): UIWindow {
            return block(scope)
        }
    }
}

@PublishedApi
internal fun <KeyType : NavigationKey> createUIWindowNavigationBinding(
    keyType: KClass<KeyType>,
    destinationType: KClass<UIWindow>,
    keySerializer: KSerializer<KeyType>,
    provider: UIWindowProvider<KeyType>,
): NavigationBinding<KeyType, UIWindow> {
    return UIWindowNavigationBinding(
        keyType = keyType,
        destinationType = destinationType,
        keySerializer = keySerializer,
        windowProvider = provider,
    )
}

public inline fun <reified KeyType : NavigationKey> NavigationModuleScope.uiWindowDestination(
    keySerializer: KSerializer<KeyType>,
    provider: UIWindowProvider<KeyType>,
) {
    binding(
        createUIWindowNavigationBinding(
            keyType = KeyType::class,
            destinationType = UIWindow::class,
            keySerializer = keySerializer,
            provider = provider,
        )
    )
}