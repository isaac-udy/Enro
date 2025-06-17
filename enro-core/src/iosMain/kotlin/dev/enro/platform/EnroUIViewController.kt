package dev.enro.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.ComposeUIViewControllerConfiguration
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.EnroController
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.context.RootContext
import dev.enro.ui.LocalNavigationContext
import kotlinx.serialization.Serializable
import platform.UIKit.UIViewController

public fun EnroUIViewController(
    configure: ComposeUIViewControllerConfiguration.() -> Unit = {},
    content: @Composable () -> Unit,
): UIViewController {
    return ComposeUIViewController(
        configure,
    ) {
        val instance = remember { GenericUIViewControllerKey.asInstance() }
        val enroController = remember {
            requireNotNull(EnroController.instance) {
                "EnroController instance is not available. Ensure that Enro is properly initialized."
            }
        }
        val viewController = LocalUIViewController.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val viewModelStoreOwner = requireNotNull(LocalViewModelStoreOwner.current) {
            "LocalViewModelStoreOwner is not provided. Ensure that the composable is hosted within a ViewModelStoreOwner."
        }
        val activeChildId = remember { mutableStateOf<String?>(null) }
        val rootContext = remember {
            RootContext(
                id = "UIViewController(${instance.key::class.simpleName})" + "$@${viewController.hashCode()}",
                parent = viewController,
                controller = enroController,
                lifecycleOwner = lifecycleOwner,
                viewModelStoreOwner = viewModelStoreOwner,
                defaultViewModelProviderFactory = viewModelStoreOwner as HasDefaultViewModelProviderFactory,
                activeChildId = activeChildId,
            ).apply {
                viewController.internalNavigationContext = this
            }
        }

        CompositionLocalProvider(
            LocalNavigationContext provides rootContext
        ) {
            content()
        }
    }
}

@Serializable
internal object GenericUIViewControllerKey : NavigationKey
