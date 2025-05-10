package dev.enro.destination.ios

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.core.NavigationContext
import dev.enro.core.compose.LocalNavigationHandle
import dev.enro.core.compose.destination.EnroLocalSavedStateRegistryOwner
import dev.enro.core.controller.enroNavigationController
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.OnNavigationContextCreated
import dev.enro.core.window.navigationContext
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import platform.UIKit.UIApplication

@Composable
internal fun ProvideNavigationContextForUIViewController(
    content: @Composable () -> Unit
) {
    val uiViewController = LocalUIViewController.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val saveableStateRegistry = LocalSaveableStateRegistry.current
    val viewModelStoreOwner = requireNotNull(LocalViewModelStoreOwner.current)
    val navigationContext = remember {
        val instruction = requireNotNull(uiViewController.navigationInstruction) {
            "TODO better error; no instruction, can't have been constructed properly"
        }

        val savedStateRegistry = object : SavedStateRegistryOwner, ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore get() = viewModelStoreOwner.viewModelStore
            private val lifecycleRegistry = LifecycleRegistry(this)
            override val lifecycle: Lifecycle get() = lifecycleRegistry

            private val savedStateRegistryController = SavedStateRegistryController.create(this)
            override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry

            init {
                enableSavedStateHandles()
                savedStateRegistryController.performRestore(null)
                lifecycleRegistry.currentState = Lifecycle.State.CREATED
                lifecycleOwner.lifecycleScope.launch {
                    lifecycleOwner.lifecycle.currentStateFlow.collectLatest {
                        lifecycleRegistry.currentState = it
                    }
                }
            }
        }

        NavigationContext(
            contextReference = uiViewController,
            getController = { UIApplication.sharedApplication.enroNavigationController },
            getParentContext = { uiViewController.parentViewController?.navigationContext },
            getContextInstruction = { instruction },
            getViewModelStoreOwner = { viewModelStoreOwner },
            getSavedStateRegistryOwner = { savedStateRegistry },
            getLifecycleOwner = { lifecycleOwner },
            onBoundToNavigationHandle = { }
        ).apply {
            UIApplication.sharedApplication.enroNavigationController
                .dependencyScope.get<OnNavigationContextCreated>()
                .invoke(this, null)
            uiViewController.navigationContext = this
        }
    }

    CompositionLocalProvider(
        LocalNavigationHandle provides navigationContext.navigationHandle,
        EnroLocalSavedStateRegistryOwner provides navigationContext.savedStateRegistryOwner,
    ) {
        content()
    }
}