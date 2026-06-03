/**
 * Enro Recipe: Staggered Per-Element Animations
 *
 * Demonstrates Modifier.animateNavigationEnterExit and NavigationAnimatedVisibility
 * for animating individual elements within a destination, independently of (and
 * alongside) the destination-level transition.
 *
 * Two examples:
 *  1. A custom dialog overlay where the scrim, card, and action buttons each
 *     animate on their own timing.
 *  2. A regular push destination where a banner inside the screen has its own
 *     enter/exit on top of the standard screen transition.
 */
package dev.enro.recipes.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.enro.*
import dev.enro.annotations.NavigationDestination
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.animation.NavigationAnimatedVisibility
import dev.enro.ui.animation.animateNavigationEnterExit
import dev.enro.ui.navigationDestination
import dev.enro.ui.rememberNavigationContainer
import dev.enro.ui.scenes.directOverlay
import kotlinx.serialization.Serializable

@Serializable
object StaggeredAnimationsRecipe : NavigationKey

@Serializable
data object StaggeredHome : NavigationKey

@Serializable
data class StaggeredDialog(val title: String, val message: String) : NavigationKey

@Serializable
data class StaggeredScreen(val label: String) : NavigationKey

@Composable
@NavigationDestination(StaggeredAnimationsRecipe::class)
fun StaggeredAnimationsRecipeScreen() {
    val navigation = navigationHandle<StaggeredAnimationsRecipe>()
    RecipeScaffold(
        title = "Staggered Animations",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(StaggeredHome.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(StaggeredHome::class)
fun StaggeredHomeDestination() {
    val navigation = navigationHandle<StaggeredHome>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Per-element animations",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "These examples animate parts of a destination on their own " +
                "timing — separate from (and on top of) the destination-level " +
                "transition.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Button(
            onClick = {
                navigation.open(
                    StaggeredDialog(
                        title = "Delete file?",
                        message = "Scrim, card, and buttons each animate on their own timing.",
                    )
                )
            },
        ) {
            Text("Show staggered dialog overlay")
        }

        Button(onClick = { navigation.open(StaggeredScreen("Details")) }) {
            Text("Open screen with animated banner")
        }
    }
}

/**
 * Example 1: A directOverlay() destination whose scrim, card, and buttons
 * are animated independently using `animateNavigationEnterExit` and
 * `NavigationAnimatedVisibility`.
 *
 * The destination itself uses [directOverlay] so Enro doesn't apply a
 * default transition — every visible motion below is composed from the
 * inner pieces hooking into the destination's AnimatedVisibilityScope.
 */
@NavigationDestination(StaggeredDialog::class)
val staggeredDialogDestination: NavigationDestinationProvider<StaggeredDialog> = navigationDestination(
    metadata = {
        directOverlay()
    },
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // Scrim: long, gentle fade. Clicking dismisses.
        Box(
            modifier = Modifier
                .matchParentSize()
                .animateNavigationEnterExit(
                    enter = fadeIn(tween(durationMillis = 320)),
                    exit = fadeOut(tween(durationMillis = 220)),
                )
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(interactionSource = null, indication = null) { navigation.requestClose() },
        )

        // Card: slide up + scale + fade, snappier than the scrim.
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .animateNavigationEnterExit(
                    enter = slideInVertically(tween(260)) { it / 4 } +
                        fadeIn(tween(220)) +
                        scaleIn(animationSpec = tween(260), initialScale = 0.92f),
                    exit = slideOutVertically(tween(180)) { it / 4 } +
                        fadeOut(tween(140)) +
                        scaleOut(animationSpec = tween(180), targetScale = 0.96f),
                ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(navigation.key.title, style = MaterialTheme.typography.titleLarge)
                Text(navigation.key.message, style = MaterialTheme.typography.bodyMedium)

                // Buttons: NavigationAnimatedVisibility for a delayed reveal that
                // still stays tied to the destination's enter/exit.
                NavigationAnimatedVisibility(
                    enter = fadeIn(tween(durationMillis = 220, delayMillis = 140)),
                    exit = fadeOut(tween(durationMillis = 100)),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { navigation.close() }) { Text("Cancel") }
                        Button(onClick = { navigation.close() }) { Text("Delete") }
                    }
                }
            }
        }
    }
}

/**
 * Example 2: A regular pushed destination (no overlay metadata, so it uses
 * the container's normal transition) that adds a per-element animation to
 * an in-screen banner.
 *
 * The screen slides in/out with the default container animation. The banner
 * inside it has its own slide-from-side + fade, layered on top.
 */
@Composable
@NavigationDestination(StaggeredScreen::class)
fun StaggeredScreenDestination() {
    val navigation = navigationHandle<StaggeredScreen>()
    var showBanner by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Screen: ${navigation.key.label}", style = MaterialTheme.typography.titleMedium)

        if (showBanner) {
            val slideHeight = LocalDensity.current.run { 64.dp.toPx().toInt() }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateNavigationEnterExit(
                        // Slides in from the bottom + fades, while the screen
                        // itself is doing its standard push transition.
                        enter = slideInVertically(tween(360)) { slideHeight } +
                            fadeIn(tween(durationMillis = 300, delayMillis = 80)),
                        exit = slideOutVertically(tween(220)) { slideHeight } +
                            fadeOut(tween(160)),
                    ),
            ) {
                Text(
                    "I'm a banner with my own animation, on top of the screen's transition.",
                    modifier = Modifier.padding(16.dp),
                )
            }
        }

        Text(
            text = "Push another screen below — watch the banner animate alongside the " +
                "screen-level transition, on its own curve.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Button(onClick = { navigation.open(StaggeredScreen("Deeper")) }) {
            Text("Go deeper")
        }
        Button(onClick = { showBanner = !showBanner }) {
            Text(if (showBanner) "Hide banner" else "Show banner")
        }
        Button(onClick = { navigation.close() }) {
            Text("Go back")
        }
    }
}
