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

/**
 * A single navigable section in [AdaptiveNavigationSceneDecorator]. Pair a
 * [NavigationKey] (the root destination of the section) with a label and an
 * icon for the sidebar / bottom-bar UI.
 */
internal data class NavigationSection(
    val key: NavigationKey,
    val label: String,
    val icon: ImageVector,
)

/**
 * A [SceneDecoratorStrategy] that wraps the current scene in either a
 * vertical sidebar (on wide windows) or a Material 3 bottom navigation bar
 * (on narrow windows). Tapping a section replaces the container's backstack
 * with that section's root key so the section's destination renders inside
 * the chrome.
 *
 * **How this preserves the chrome across section transitions**:
 *
 * The wrapper's `key` is derived from the inner scene's identity, so
 * `NavigationDisplay`'s outer `AnimatedContent` treats each section as a
 * different `SceneIdentity` and runs whatever transition the display /
 * destination is configured with — push, pop, predictive back, per-scene
 * `TransitionKey` overrides, all of it works automatically. The inner
 * scene's content (`scene.content()`) sits directly inside the wrapper's
 * content and rides those transitions.
 *
 * To keep the chrome itself from re-animating (it's just the
 * sidebar/bottom-bar; tapping a section shouldn't fade or slide the
 * navigation chrome), the sidebar and bottom-bar composables are wrapped
 * in `movableContentOf` and rendered inside a `Modifier.sharedElement`
 * with a stable key. Compose's invariant is "movable content is composed
 * in exactly one place at a time": the outgoing scene's composition
 * holds an empty placeholder box (still keyed for the shared element);
 * the incoming scene's composition is the one that actually invokes the
 * movable chrome. The `SharedTransitionScope` bridges the chrome's
 * bounds from old to new, so visually it never moves.
 *
 * This pattern mirrors Nav3's `ResponsiveNavigationSceneDecorator` and
 * is the recommended way to add "stable" chrome around an
 * `AnimatedContent`-driven scene model. The alternative — giving the
 * wrapper a constant `key` so the outer slot never re-animates — keeps
 * the chrome stable but also robs the inner content of all the
 * configurable transitions, so we don't use it here.
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

        // Remember the movable chrome once for the lifetime of the strategy
        // composition. These compose in exactly one place at a time even when
        // both the outgoing and incoming wrapper compositions invoke them
        // during an AnimatedContent transition — the `isMovableContentCaller`
        // gate below picks the incoming one.
        val movableSidebar = remember { movableContentOf { Sidebar(sections) } }
        val movableBottomBar = remember { movableContentOf { BottomBar(sections) } }

        return remember(scene, showSidebar) {
            object : NavigationScene by scene {
                // Derive key from the inner scene so NavigationDisplay's
                // outer AnimatedContent runs a normal transition between
                // sections / between push & pop within a section, using
                // the configured NavigationAnimations and any per-scene
                // metadata transition overrides.
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
