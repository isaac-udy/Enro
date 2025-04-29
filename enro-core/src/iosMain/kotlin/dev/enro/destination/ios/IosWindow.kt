package dev.enro.destination.ios

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.PredictiveBackHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.savedState
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.compose.LocalNavigationHandle
import dev.enro.core.compose.destination.EnroLocalSavedStateRegistryOwner
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.OnNavigationContextCreated
import dev.enro.core.leafContext
import dev.enro.core.requestClose
import platform.UIKit.UIWindow

public abstract class IosWindow {
    public lateinit var window: UIWindow
    internal lateinit var instruction: NavigationInstruction.Open<*>

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    internal fun ApplyLocals(
        controller: NavigationController,
        content: @Composable () -> Unit
    ) {
        val context = remember {
            val owners = object : SavedStateRegistryOwner, ViewModelStoreOwner {
                private val savedStateRegistryController = SavedStateRegistryController.create(this)
                override val savedStateRegistry: SavedStateRegistry =
                    savedStateRegistryController.savedStateRegistry
                override val viewModelStore: ViewModelStore = ViewModelStore()
                private val lifecycleRegistry = LifecycleRegistry(this)
                override val lifecycle: Lifecycle get() = lifecycleRegistry

                init {
                    enableSavedStateHandles()
                    savedStateRegistryController.performRestore(null)
                    lifecycleRegistry.currentState = Lifecycle.State.RESUMED
                }
            }
            NavigationContext(
                contextReference = window,
                getController = { controller },
                getParentContext = { null },
                getArguments = { savedState() },
                getViewModelStoreOwner = { owners },
                getSavedStateRegistryOwner = { owners },
                getLifecycleOwner = { owners },
                onBoundToNavigationHandle = { }
            ).apply {
                controller.dependencyScope.get<OnNavigationContextCreated>()
                    .invoke(this, null)
            }
        }

        PredictiveBackHandler { progress ->
            progress.collect {}
            context.leafContext().navigationHandle.requestClose()
        }
        CompositionLocalProvider(
            EnroLocalSavedStateRegistryOwner provides context.savedStateRegistryOwner,
            LocalNavigationHandle provides context.navigationHandle,
            LocalSaveableStateRegistry provides SaveableStateRegistry(
                restoredValues = mapOf(),
                canBeSaved = { key -> true },
            )
        ) {
            content()
        }
    }
}