package dev.enro.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.ComposeUIViewControllerConfiguration
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
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
    val owners = object : SavedStateRegistryOwner, ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
        private val savedStateRegistryController = SavedStateRegistryController.create(this)
        override val savedStateRegistry: SavedStateRegistry =
            savedStateRegistryController.savedStateRegistry
        override val viewModelStore: ViewModelStore = ViewModelStore()
        private val lifecycleRegistry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = lifecycleRegistry
        val self = this
        init {
            enableSavedStateHandles()
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        }
        override val defaultViewModelProviderFactory: ViewModelProvider.Factory get() {
            return viewModelFactory {  }
        }

        override val defaultViewModelCreationExtras: CreationExtras get() {
            return MutableCreationExtras().apply {
                set(SAVED_STATE_REGISTRY_OWNER_KEY, self)
                set(VIEW_MODEL_STORE_OWNER_KEY, self)
            }
        }
    }
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
                defaultViewModelProviderFactory = owners ,
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
