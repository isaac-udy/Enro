@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
package dev.enro.destination.ios

import androidx.compose.runtime.Composable
import androidx.compose.ui.uikit.ComposeUIViewControllerConfiguration
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewController
import platform.objc.OBJC_ASSOCIATION_RETAIN_NONATOMIC
import platform.objc.objc_getAssociatedObject
import platform.objc.objc_setAssociatedObject


@OptIn(ExperimentalForeignApi::class)
private val IsEnroViewControllerKey = kotlinx.cinterop.staticCFunction<Unit> {}

@OptIn(ExperimentalForeignApi::class)
public var UIViewController.isEnroViewController: Boolean
    get() {
        return objc_getAssociatedObject(
            this,
            IsEnroViewControllerKey
        ) as? Boolean ?: false
    }
    private set(value) {
        objc_setAssociatedObject(
            `object` = this,
            key = IsEnroViewControllerKey,
            value = value,
            policy = OBJC_ASSOCIATION_RETAIN_NONATOMIC
        )
    }

public fun EnroComposeUIViewController(content: @Composable () -> Unit): UIViewController {
    return EnroComposeUIViewController(configure = {}, content = content)
}

public fun EnroComposeUIViewController(
    configure: ComposeUIViewControllerConfiguration.() -> Unit = {},
    content: @Composable () -> Unit
): UIViewController {
    val viewController = ComposeUIViewController (
        configure = {
            configure()
        },
    ) {
        ProvideNavigationContextForUIViewController(content)
    }
    viewController.isEnroViewController = true
    return viewController
}