package dev.enro.destination.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
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
import dev.enro.core.NavigationInstruction
import dev.enro.core.compose.LocalNavigationHandle
import dev.enro.core.compose.destination.EnroLocalSavedStateRegistryOwner
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.OnNavigationContextCreated
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

public abstract class WebWindow {
    internal lateinit var instruction: NavigationInstruction.Open<*>
    private var navigationContext: NavigationContext<WebWindow>? = null

    @Composable
    internal fun ApplyLocals(
        controller: NavigationController,
        content: @Composable () -> Unit
    ) {
        val lifecycleOwner = LocalLifecycleOwner.current
        val viewModelStoreOwner = requireNotNull(LocalViewModelStoreOwner.current)
        navigationContext = remember {
            require(::instruction.isInitialized) {
                "WebWindow's instruction property was not initialised before being used"
            }
            require(navigationContext == null) {
                "WebWindow's navigationContext property was already initialised"
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
                contextReference = this,
                getController = { controller },
                getParentContext = { null },
                getContextInstruction = { instruction },
                getViewModelStoreOwner = { viewModelStoreOwner },
                getSavedStateRegistryOwner = { savedStateRegistry },
                getLifecycleOwner = { lifecycleOwner },
                onBoundToNavigationHandle = { }
            ).apply {
                controller.dependencyScope.get<OnNavigationContextCreated>()
                    .invoke(this, null)
            }
        }

        CompositionLocalProvider(
            EnroLocalSavedStateRegistryOwner provides requireNotNull(navigationContext).savedStateRegistryOwner,
            LocalViewModelStoreOwner provides requireNotNull(navigationContext).viewModelStoreOwner,
            LocalNavigationHandle provides requireNotNull(navigationContext).navigationHandle,
            LocalSaveableStateRegistry provides SaveableStateRegistry(
                restoredValues = mapOf(),
                canBeSaved = { key -> true },
            )
        ) {
            content()
        }
    }

    @Composable
    public abstract fun Render()
}