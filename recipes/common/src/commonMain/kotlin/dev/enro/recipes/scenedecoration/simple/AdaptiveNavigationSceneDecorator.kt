package dev.enro.recipes.scenedecoration.simple

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.ui.*

/** A section of [AdaptiveNavigationSceneDecorator]. [key] is the root destination of the section. */
internal data class NavigationSection(
    val key: NavigationKey,
    val label: String,
    val icon: ImageVector,
)

/**
 * A [SceneDecoratorStrategy] that wraps the scene in a sidebar on a wide
 * window and in a Material 3 bottom navigation bar on a narrow window. A tap
 * on a section replaces the container's backstack with the root key of the
 * section.
 *
 * The decorated scene derives `key` from the wrapped scene, so
 * `NavigationDisplay` runs the configured transition when the wrapped scene
 * changes. A decorated scene with a constant `key` would run no transition.
 *
 * The sidebar and the bottom bar must not animate with the scene. Each is
 * `movableContentOf` content inside a `Modifier.sharedElement` slot with a
 * constant shared content key. Movable content is composed in one place at
 * a time: the entering scene composes the content, and the exiting scene
 * composes an empty slot. The `SharedTransitionScope` keeps the bounds of
 * the slot constant during the transition.
 *
 * Nav3 equivalent: `ResponsiveNavigationSceneDecorator`.
 */
internal class AdaptiveNavigationSceneDecorator(
    private val sections: List<NavigationSection>,
    private val sidebarBreakpointDp: Int = 600,
) : SceneDecoratorStrategy {

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    override fun SceneDecoratorStrategyScope.decorateScene(scene: NavigationScene): NavigationScene {
        val width = with(LocalDensity.current) {
            LocalWindowInfo.current.containerSize.toSize().width.toDp()
        }
        val showSidebar = width >= sidebarBreakpointDp.dp

        // During a transition the entering scene and the exiting scene are both
        // composed. `isMovableContentCaller` below is true only in the entering
        // scene, so one scene composes the movable chrome.
        val movableSidebar = remember { movableContentOf { Sidebar(sections) } }
        val movableBottomBar = remember { movableContentOf { BottomBar(sections) } }

        return remember(scene, showSidebar) {
            object : NavigationScene by scene {
                // `key` changes when the wrapped scene changes, so
                // NavigationDisplay runs the configured transition.
                override val key: Any = scene::class to scene.key

                override val content: @Composable () -> Unit = {
                    val animatedScope = LocalNavigationAnimatedVisibilityScope.current
                    val sharedScope = LocalNavigationSharedTransitionScope.current
                    val isMovableContentCaller =
                        animatedScope.transition.targetState == EnterExitState.Visible

                    with(sharedScope) {
                        if (showSidebar) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .width(SidebarWidth)
                                        .fillMaxHeight()
                                        .sharedElement(
                                            sharedContentState = rememberSharedContentState(SidebarSharedKey),
                                            animatedVisibilityScope = animatedScope,
                                        ),
                                ) {
                                    if (isMovableContentCaller) movableSidebar()
                                }
                                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                    scene.content()
                                }
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    scene.content()
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .sharedElement(
                                            sharedContentState = rememberSharedContentState(BottomBarSharedKey),
                                            animatedVisibilityScope = animatedScope,
                                        ),
                                ) {
                                    if (isMovableContentCaller) movableBottomBar()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private companion object {
        val SidebarWidth = 220.dp
        const val SidebarSharedKey = "AdaptiveNavigationSceneDecorator.sidebar"
        const val BottomBarSharedKey = "AdaptiveNavigationSceneDecorator.bottombar"
    }
}

@Composable
private fun Sidebar(sections: List<NavigationSection>) {
    val container = LocalNavigationContainer.current
    val currentKey = container.backstack.firstOrNull()?.key
    Surface(
        modifier = Modifier.fillMaxSize(),
        tonalElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Sections",
                modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 8.dp),
            )
            sections.forEach { section ->
                SectionButton(
                    section = section,
                    isSelected = currentKey == section.key,
                    onClick = { container.navigateToSection(section) },
                )
            }
        }
    }
}

@Composable
private fun SectionButton(
    section: NavigationSection,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(section.icon, contentDescription = null)
            Text(section.label)
        }
    }
    if (isSelected) {
        FilledTonalButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { content() }
    } else {
        OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { content() }
    }
}

@Composable
private fun BottomBar(sections: List<NavigationSection>) {
    val container = LocalNavigationContainer.current
    val currentKey = container.backstack.firstOrNull()?.key
    NavigationBar {
        sections.forEach { section ->
            NavigationBarItem(
                selected = currentKey == section.key,
                onClick = { container.navigateToSection(section) },
                icon = { Icon(section.icon, contentDescription = null) },
                label = { Text(section.label) },
            )
        }
    }
}

private fun NavigationContainerState.navigateToSection(section: NavigationSection) {
    if (backstack.firstOrNull()?.key == section.key && backstack.size == 1) return
    updateBackstack {
        backstackOf(section.key.asInstance())
    }
}
