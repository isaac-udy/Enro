package dev.enro.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.ComposeUIViewControllerConfiguration
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.EnroController
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.context.RootContext
import dev.enro.handle.RootNavigationHandle
import dev.enro.handle.getOrCreateNavigationHandleHolder
import dev.enro.ui.LocalNavigationContext
import dev.enro.ui.LocalNavigationHandle
import dev.enro.viewmodel.EnroWrappedViewModelStoreOwner
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
        val wrappedViewModelStoreOwner = remember(viewModelStoreOwner) {
            EnroWrappedViewModelStoreOwner(
                controller = enroController,
                viewModelStoreOwner = viewModelStoreOwner,
                savedStateRegistryOwner = null
            )
        }
        val activeChildId = remember { mutableStateOf<String?>(null) }
        val (context, navigationHandle) = remember(wrappedViewModelStoreOwner) {
            val context = RootContext(
                id = "UIViewController(${instance.key::class.simpleName})" + "$@${viewController.hashCode()}",
                parent = viewController,
                controller = enroController,
                lifecycleOwner = lifecycleOwner,
                viewModelStoreOwner = wrappedViewModelStoreOwner,
                defaultViewModelProviderFactory = wrappedViewModelStoreOwner,
                activeChildId = activeChildId,
            )
            viewController.internalNavigationContext = context

            val instance = instance
            val holder = viewModelStoreOwner.getOrCreateNavigationHandleHolder {
                RootNavigationHandle(
                    instance = instance,
                    savedStateHandle = createSavedStateHandle(),
                )
            }
            val navigationHandle = holder.navigationHandle
            require(navigationHandle is RootNavigationHandle)
            navigationHandle.bindContext(context)

            return@remember context to navigationHandle
        }

        DisposableEffect(context) {
            enroController.rootContextRegistry.register(context)
            onDispose {
                enroController.rootContextRegistry.unregister(context)
            }
        }

        CompositionLocalProvider(
            LocalNavigationContext provides context,
            LocalNavigationHandle provides navigationHandle,
            LocalViewModelStoreOwner provides wrappedViewModelStoreOwner,
        ) {
            content()
        }
    }
}

@Serializable
internal object GenericUIViewControllerKey : NavigationKey
