@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@file:OptIn(ExperimentalComposeUiApi::class)

package dev.enro.tests.application

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.PredictiveBackHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.savedState
import dev.enro.animation.direction
import dev.enro.annotations.NavigationComponent
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.compose.LocalNavigationHandle
import dev.enro.core.compose.destination.EnroLocalSavedStateRegistryOwner
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.NavigationModuleScope
import dev.enro.core.controller.createNavigationController
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.OnNavigationContextCreated
import dev.enro.core.leafContext
import dev.enro.core.requestClose
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

@NavigationComponent
object EnroExampleApp

fun MainViewController(
    generatedModule: Any,
): UIViewController {
    val controller = createNavigationController(UIApplication.sharedApplication) {
        generatedModule as (NavigationModuleScope) -> Unit
        generatedModule.invoke(this)
        animations {
            direction(
                direction = NavigationDirection.Push,
                entering = fadeIn() + slideInHorizontally { it / 3 },
                exiting = slideOutHorizontally { -it / 6 },
                returnEntering = slideInHorizontally { -it / 6 },
                returnExiting = fadeOut() + slideOutHorizontally { it / 3 }
            )
        }
    }
    return ComposeUIViewController {
        ApplyLocals(controller) {
            val container = rememberNavigationContainer(
                root = SelectDestination,
                emptyBehavior = EmptyBehavior.Action {
                    true
                },
            )

            Column {
                IconButton(
                    onClick = {
                        container.context.leafContext().navigationHandle.requestClose()
                    }
                ) {
                    Icon(Icons.Default.ArrowBack, null)
                }
                Box(
                    modifier = Modifier
                        .background(Color.LightGray)
                        .fillMaxSize()
                ) {
                    container.Render()
                }
            }
        }
    }
}


@Composable
internal fun ApplyLocals(
    controller: NavigationController,
    content: @Composable () -> Unit
) {
    val context = remember {
        val owners = object : SavedStateRegistryOwner, ViewModelStoreOwner {
            private val savedStateRegistryController = SavedStateRegistryController.create(this)
            override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry
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
            contextReference = UIApplication.sharedApplication,
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
