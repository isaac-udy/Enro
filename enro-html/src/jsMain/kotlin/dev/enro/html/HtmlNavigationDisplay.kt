package dev.enro.html

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import dev.enro.NavigationKey
import dev.enro.requestClose
import dev.enro.ui.LocalNavigationContainer
import dev.enro.ui.LocalNavigationContext
import dev.enro.ui.NavigationContainerState
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneState
import dev.enro.ui.NavigationSceneStrategy
import dev.enro.ui.SceneDecoratorStrategy
import dev.enro.ui.get
import dev.enro.ui.rememberNavigationSceneState
import dev.enro.ui.scenes.DirectOverlaySceneStrategy
import dev.enro.ui.scenes.SinglePaneSceneStrategy
import dev.enro.viewmodel.getNavigationHandle
import org.jetbrains.compose.web.css.opacity
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLElement

/**
 * Renders a [NavigationContainerState] with Compose HTML.
 *
 * The current scene is replaced without an animation. Overlay scenes render after it, in
 * backstack order: [htmlDialog] destinations in a native `<dialog>`, and
 * [dev.enro.ui.scenes.directOverlay] destinations directly. A direct overlay that sets
 * [dev.enro.ui.scenes.directOverlayWithFade] fades in and out. A closed overlay stays in the DOM
 * until its CSS transitions finish.
 *
 * Browser back and forward are handled by [dev.enro.ui.InstallWebHistoryPlugin], not by this
 * display.
 */
@Composable
public fun HtmlNavigationDisplay(
    state: NavigationContainerState,
    sceneStrategy: NavigationSceneStrategy = remember {
        NavigationSceneStrategy.from(
            HtmlDialogSceneStrategy(),
            DirectOverlaySceneStrategy(),
            SinglePaneSceneStrategy(),
        )
    },
    sceneDecoratorStrategies: List<SceneDecoratorStrategy> = emptyList(),
) {
    DisposableEffect(state) {
        state.context.parent.registerVisibility(state.context, true)
        onDispose {
            state.context.parent.registerVisibility(state.context, false)
        }
    }

    val sceneStateRef = remember { mutableStateOf<NavigationSceneState?>(null) }
    val onBack: () -> Unit = remember(state) {
        {
            val current = sceneStateRef.value?.currentScene
            if (current != null) {
                val previousIds = current.previousEntries.map { it.instance.id }.toSet()
                state.context.children
                    .filter { it.id !in previousIds }
                    .forEach { it.getNavigationHandle<NavigationKey>().requestClose() }
            }
        }
    }
    val sceneState = rememberNavigationSceneState(
        containerState = state,
        sceneStrategy = sceneStrategy,
        onBack = onBack,
        sceneDecoratorStrategies = sceneDecoratorStrategies,
    )
    sceneStateRef.value = sceneState

    CompositionLocalProvider(
        LocalNavigationContainer provides state,
        LocalNavigationContext provides state.context,
    ) {
        val currentScene = sceneState.currentScene
        key(currentScene::class, currentScene.key) {
            currentScene.content()
        }
        HtmlOverlayScenes(sceneState.overlayScenes)
    }
}

@Composable
private fun HtmlOverlayScenes(overlayScenes: List<NavigationScene.Overlay>) {
    val rendered = remember { mutableStateMapOf<Any, NavigationScene.Overlay>() }
    val keysInOrder = remember { mutableStateListOf<Any>() }

    overlayScenes.forEach { scene ->
        if (scene.key !in rendered) keysInOrder.add(scene.key)
        rendered[scene.key] = scene
    }
    val activeKeys = overlayScenes.map { it.key }.toSet()

    keysInOrder.toList().forEach { sceneKey ->
        val scene = rendered[sceneKey] ?: return@forEach
        key(sceneKey) {
            HtmlOverlayScene(
                scene = scene,
                visible = sceneKey in activeKeys,
                onRemoved = {
                    rendered.remove(sceneKey)
                    keysInOrder.remove(sceneKey)
                },
            )
        }
    }
}

@Composable
private fun HtmlOverlayScene(
    scene: NavigationScene.Overlay,
    visible: Boolean,
    onRemoved: () -> Unit,
) {
    val currentOnRemoved by rememberUpdatedState(onRemoved)
    var element by remember { mutableStateOf<HTMLElement?>(null) }
    when (scene) {
        is HtmlDialogScene -> scene.Render(visible = visible, onElement = { element = it })
        else -> FadingOverlay(scene = scene, visible = visible, onElement = { element = it })
    }
    LaunchedEffect(visible) {
        if (visible) return@LaunchedEffect
        element?.awaitTransitions()
        scene.onRemove()
        currentOnRemoved()
    }
}

@Composable
private fun FadingOverlay(
    scene: NavigationScene.Overlay,
    visible: Boolean,
    onElement: (HTMLElement?) -> Unit,
) {
    val fadeMillis = scene.entries.lastOrNull()?.metadata?.get(DirectOverlaySceneStrategy.OverlayFadeKey)
    if (fadeMillis == null) {
        scene.content()
        return
    }
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // The element must be styled at opacity 0 for a frame, or the fade-in does not run.
        withFrameMillis { }
        entered = true
    }
    Div(attrs = {
        style {
            property("transition", "opacity ${fadeMillis}ms")
            opacity(if (entered && visible) 1 else 0)
        }
        ref { element ->
            onElement(element)
            onDispose { onElement(null) }
        }
    }) {
        scene.content()
    }
}

internal fun NavigationContainerState.requestClose(entry: NavigationDestination<NavigationKey>) {
    context.children
        .firstOrNull { it.id == entry.instance.id }
        ?.getNavigationHandle<NavigationKey>()
        ?.requestClose()
}
