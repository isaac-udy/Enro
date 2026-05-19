package dev.enro.recipes.scenedecoration.complex

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.recipes.scenedecoration.complex.destinations.CartOverlay
import dev.enro.ui.LocalNavigationAnimatedVisibilityScope
import dev.enro.ui.LocalNavigationContainer
import dev.enro.ui.LocalNavigationSharedTransitionScope
import dev.enro.ui.NavigationContainerState
import dev.enro.ui.NavigationScene
import dev.enro.ui.SceneDecoratorStrategy
import dev.enro.ui.SceneDecoratorStrategyScope

internal data class ShellSection(
    val key: NavigationKey,
    val label: String,
    val icon: ImageVector,
)

/**
 * The Shell's chrome wrapper. Composition per breakpoint:
 *
 *   - **Wide / Medium**: top bar (logo + search + cart) above a Row of
 *     left rail + content.
 *   - **Mobile**: top bar (logo + cart) above content; optional bottom
 *     chrome (search + nav row) below.
 *
 * The mobile bottom chrome hides when the top entry has `fullScreen()` /
 * `leftPane()` / `rightPane()` metadata **unless** the destination is the
 * only thing on the backstack (section root) — in that case the user still
 * needs the bottom nav to switch sections, so it stays visible.
 *
 * The chrome elements are wrapped in `movableContentOf` and rendered
 * through a `Modifier.sharedElement` slot gated by
 * `transition.targetState == EnterExitState.Visible`. Outer
 * `AnimatedContent` runs the configured transitions for inner content
 * (because the wrapper's `key` derives from the inner scene's identity);
 * the chrome stays visually pinned because movable content is composed
 * only in the incoming composition and shared-element bridges its bounds
 * across the transition. Same Nav3-inspired pattern as the simple recipe's
 * `AdaptiveNavigationSceneDecorator`.
 */
internal class ShellSceneDecorator(
    private val sections: List<ShellSection>,
) : SceneDecoratorStrategy {

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    override fun SceneDecoratorStrategyScope.decorateScene(scene: NavigationScene): NavigationScene {
        val breakpoint = rememberShellBreakpoint()
        val topEntry = scene.entries.lastOrNull()
        val claimsFullArea = topEntry?.let {
            it.isFullScreen || it.isLeftPane || it.isRightPane
        } ?: false
        val isSectionRoot = scene.previousEntries.isEmpty()
        val showBottomChrome = breakpoint == ShellBreakpoint.Mobile &&
            (isSectionRoot || !claimsFullArea)

        val desktopTopBar = remember { movableContentOf { ShellDesktopTopBar() } }
        val mobileTopBar = remember { movableContentOf { ShellMobileTopBar() } }
        val leftRail = remember(sections) { movableContentOf { ShellLeftRail(sections) } }
        val bottomChrome = remember(sections) { movableContentOf { ShellBottomChrome(sections) } }

        return remember(scene, breakpoint, showBottomChrome) {
            object : NavigationScene by scene {
                override val key: Any = scene::class to scene.key

                override val content: @Composable () -> Unit = {
                    val animatedScope = LocalNavigationAnimatedVisibilityScope.current
                    val sharedScope = LocalNavigationSharedTransitionScope.current
                    val isMovableContentCaller =
                        animatedScope.transition.targetState == EnterExitState.Visible

                    with(sharedScope) {
                        if (breakpoint == ShellBreakpoint.Mobile) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .sharedElement(
                                            sharedContentState = rememberSharedContentState(MobileTopBarKey),
                                            animatedVisibilityScope = animatedScope,
                                        ),
                                ) {
                                    if (isMovableContentCaller) mobileTopBar()
                                }
                                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    scene.content()
                                }
                                if (showBottomChrome) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .sharedElement(
                                                sharedContentState = rememberSharedContentState(BottomChromeKey),
                                                animatedVisibilityScope = animatedScope,
                                            ),
                                    ) {
                                        if (isMovableContentCaller) bottomChrome()
                                    }
                                }
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .sharedElement(
                                            sharedContentState = rememberSharedContentState(DesktopTopBarKey),
                                            animatedVisibilityScope = animatedScope,
                                        ),
                                ) {
                                    if (isMovableContentCaller) desktopTopBar()
                                }
                                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .width(LeftRailWidth)
                                            .fillMaxHeight()
                                            .sharedElement(
                                                sharedContentState = rememberSharedContentState(LeftRailKey),
                                                animatedVisibilityScope = animatedScope,
                                            ),
                                    ) {
                                        if (isMovableContentCaller) leftRail()
                                    }
                                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                        scene.content()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private companion object {
        val LeftRailWidth = 220.dp
        const val DesktopTopBarKey = "ShellSceneDecorator.desktopTopBar"
        const val MobileTopBarKey = "ShellSceneDecorator.mobileTopBar"
        const val LeftRailKey = "ShellSceneDecorator.leftRail"
        const val BottomChromeKey = "ShellSceneDecorator.bottomChrome"
    }
}

@Composable
private fun ShellDesktopTopBar() {
    val container = LocalNavigationContainer.current
    Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Shell", style = MaterialTheme.typography.titleMedium)
            ShellSearchField(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                container.execute(NavigationOperation.Open(CartOverlay.asInstance()))
            }) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart")
            }
        }
    }
}

@Composable
private fun ShellMobileTopBar() {
    val container = LocalNavigationContainer.current
    Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Shell",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
            )
            IconButton(onClick = {
                container.execute(NavigationOperation.Open(CartOverlay.asInstance()))
            }) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart")
            }
        }
    }
}

@Composable
private fun ShellLeftRail(sections: List<ShellSection>) {
    val container = LocalNavigationContainer.current
    val sectionKey = container.backstack.firstOrNull()?.key
    Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Sections",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
            )
            sections.forEach { section ->
                ShellRailItem(
                    section = section,
                    isSelected = sectionKey == section.key,
                    onClick = { container.navigateToShellSection(section) },
                )
            }
        }
    }
}

@Composable
private fun ShellRailItem(
    section: ShellSection,
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
private fun ShellBottomChrome(sections: List<ShellSection>) {
    val container = LocalNavigationContainer.current
    val sectionKey = container.backstack.firstOrNull()?.key
    Column(modifier = Modifier.fillMaxWidth()) {
        ShellSearchField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
        NavigationBar {
            sections.forEach { section ->
                NavigationBarItem(
                    selected = sectionKey == section.key,
                    onClick = { container.navigateToShellSection(section) },
                    icon = { Icon(section.icon, contentDescription = null) },
                    label = { Text(section.label) },
                )
            }
        }
    }
}

@Composable
private fun ShellSearchField(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Filled.Search, contentDescription = null)
            Text("Search products…", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

internal fun NavigationContainerState.navigateToShellSection(section: ShellSection) {
    if (backstack.firstOrNull()?.key == section.key && backstack.size == 1) return
    updateBackstack { backstackOf(section.key.asInstance()) }
}
