@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ComposeViewport
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.savedState
import dev.enro.annotations.NavigationComponent
import dev.enro.core.NavigationContext
import dev.enro.core.compose.LocalNavigationHandle
import dev.enro.core.compose.destination.EnroLocalSavedStateRegistryOwner
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.createNavigationController
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.OnNavigationContextCreated
import dev.enro.core.leafContext
import dev.enro.core.requestClose
import dev.enro.tests.application.SelectDestination
import kotlinx.browser.document
import org.jetbrains.compose.resources.configureWebResources
import org.w3c.dom.Element

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    configureWebResources {
        resourcePathMapping { path -> "./$path" }
    }
    val controller = createNavigationController(
        document = document,
    ) {
        EnroExampleAppNavigation().invoke(this)
    }
    runCatching {
        ComposeViewport(document.body!!) {
            ApplyLocals(
                controller = controller,
                element = document.body!!,
            ) {
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
    }.onFailure {
        it.printStackTrace()
    }
}

@Composable
internal fun ApplyLocals(
    controller: NavigationController,
    element: Element,
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
            contextReference = element,
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

// Attempt at implementing a backstack
/*
    LaunchedEffect(Unit) {
        var eventListenerEnabled = true
        val eventListener: (Event) -> Unit = {
            if (eventListenerEnabled) {
                context.leafContext().navigationHandle.requestClose()
                window.history.replaceState(context.leafContext().instruction.instructionId.toJsString(), "example", "#${context.leafContext().instruction.instructionId}")
            }
        }
        window.addEventListener("popstate", eventListener)
        context.containerManager.activeContainerFlow
            .flatMapLatest { container ->
                container?.backstackFlow?.map {
                    container to it
                } ?: flowOf(null to null)
            }
            .collectLatest { (container, backstack) ->
                if (backstack == null) return@collectLatest
                eventListenerEnabled = false
                while (isActive) {
                    delay(1)
                    val last = window.history.state
                    if (last == null || last == backstack.active?.instructionId?.toJsString()) break
                    window.history.back()
                }
                if (window.history.state != backstack.active?.instructionId?.toJsString()) {
                    backstack.forEachIndexed { index, item ->
                        window.history.pushState(item.instructionId.toJsString(), "example", "#${item.instructionId}")
                    }
                }
                eventListenerEnabled = true
            }
    }
 */

