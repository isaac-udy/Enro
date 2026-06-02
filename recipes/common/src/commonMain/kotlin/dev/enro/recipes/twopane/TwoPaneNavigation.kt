/**
 * Enro Recipe: Two-Pane Scene
 *
 * Demonstrates a custom `NavigationSceneStrategy` that renders two
 * adjacent backstack entries side-by-side, gated by per-destination
 * metadata. Each destination that's willing to participate opts in
 * with `twoPane()` in its metadata; if both of the top two entries
 * opt in (and the window is wide enough), the strategy renders them
 * together.
 *
 * Modelled on the Nav3 two-pane recipe:
 * https://developer.android.com/guide/navigation/navigation-3/recipes/scenes-twopane
 *
 * The strategy itself lives in [TwoPaneScene.kt] alongside the
 * `twoPane()` metadata helper.
 *
 * Difference from a list-detail layout: there's no fixed left/right
 * role. Any two adjacent destinations that both declare `twoPane()`
 * are rendered side-by-side, with the earlier (deeper-in-the-backstack)
 * entry on the left. Destinations that don't opt in render full-screen
 * via the regular single-pane strategy.
 */
package dev.enro.recipes.twopane

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.*
import dev.enro.annotations.NavigationDestination
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.*
import dev.enro.ui.scenes.DialogSceneStrategy
import dev.enro.ui.scenes.DirectOverlaySceneStrategy
import dev.enro.ui.scenes.SinglePaneSceneStrategy
import kotlinx.serialization.Serializable

// -- Recipe root --

@Serializable
object TwoPaneRecipe : NavigationKey

// -- Demo destination keys --

@Serializable
data object TwoPaneHome : NavigationKey

@Serializable
data class TwoPaneProduct(val id: Int) : NavigationKey

@Serializable
data object TwoPaneProfile : NavigationKey

// ─────────────────────────────────────────────────────────────────────
// Recipe root
// ─────────────────────────────────────────────────────────────────────

@Composable
@NavigationDestination(TwoPaneRecipe::class)
fun TwoPaneRecipeScreen() {
    val navigation = navigationHandle<TwoPaneRecipe>()
    RecipeScaffold(
        title = "Two-Pane Scene",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(TwoPaneHome.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
            sceneStrategy = NavigationSceneStrategy.from(
                DialogSceneStrategy(),
                DirectOverlaySceneStrategy(),
                TwoPaneSceneStrategy(),
                SinglePaneSceneStrategy(),
            ),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────
// Destinations
// ─────────────────────────────────────────────────────────────────────

@NavigationDestination(TwoPaneHome::class)
val twoPaneHomeDestination: NavigationDestinationProvider<TwoPaneHome> =
    navigationDestination(
        metadata = { twoPane() },
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Home", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Open a product. On wide screens (>= 600dp) you'll see this " +
                    "Home pane and the product pane side-by-side, because both " +
                    "destinations opt in via twoPane() metadata.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = { navigation.open(TwoPaneProduct(1)) }) {
                Text("Open product 1")
            }
        }
    }

@NavigationDestination(TwoPaneProduct::class)
val twoPaneProductDestination: NavigationDestinationProvider<TwoPaneProduct> =
    navigationDestination(
        metadata = { twoPane() },
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Product ${navigation.key.id}", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Opening another product replaces the right pane on wide " +
                    "screens, or pushes a new screen on narrow screens.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = { navigation.open(TwoPaneProduct(navigation.key.id + 1)) }) {
                Text("View next product")
            }
            Button(onClick = { navigation.open(TwoPaneProfile) }) {
                Text("View profile (single-pane only)")
            }
            Button(onClick = { navigation.close() }) {
                Text("Close")
            }
        }
    }

@Composable
@NavigationDestination(TwoPaneProfile::class)
fun TwoPaneProfileDestination() {
    // No twoPane() metadata — when this destination is on top, the
    // scene strategy returns null and the single-pane strategy renders
    // it full-screen, even on wide windows.
    val navigation = navigationHandle<TwoPaneProfile>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Profile (single pane only)", style = MaterialTheme.typography.headlineSmall)
        Text(
            "This destination does not opt in to two-pane rendering. The " +
                "scene strategy falls through and this screen renders " +
                "full-screen on any window size.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = { navigation.close() }) {
            Text("Back")
        }
    }
}
