@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeViewport
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.core.NavigationContext
import dev.enro.core.compose.LocalNavigationHandle
import dev.enro.core.compose.destination.EnroLocalSavedStateRegistryOwner
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.OnNavigationContextCreated
import dev.enro.core.leafContext
import dev.enro.core.requestClose
import dev.enro.tests.application.EnroComponent
import dev.enro.tests.application.SelectDestination
import dev.enro.tests.application.installNavigationController
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.configureWebResources
import org.w3c.dom.Element

@Serializable
data class RandomOtherType(
    val thing: String,
    val otherThing: Int,
)

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    configureWebResources {
        resourcePathMapping { path -> "./$path" }
    }
    val historyPlugin = WebHistoryPlugin(window)
    val controller = EnroComponent.installNavigationController(
        document = document,
    ) {
        plugin(historyPlugin)
    }

    /*
    There's something interesting here with hrefs and window.open calls (particularly with _self target)
    I think that we probably want to somehow relate the href to a destination,
    and use window.open to drive the JS window manager. Might need a way to iterate through
    JS registered window destinations and find one that matches a URL and use that as the root?
    Doesn't really work so well with the development server as other URLs just execute get requests

    It's also worth thinking about the multi-platform implications here. On both Android, iOS and
    the web, there's the possiblity to open a new "window" within the current context (e.g. _self)
    and a new "window" in a new context (e.g. _blank); iOS uses WindowScenes for this, Android uses
    Activities with new tasks, and the web uses new windows. Not sure if this applies to Desktop,
    but it's worth thinking about.
     */
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
                LaunchedEffect(Unit) {
                    historyPlugin.rootContainer = container
                }

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
                            .background(MaterialTheme.colors.background)
                            .fillMaxWidth()
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
            getContextInstruction = { null },
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

