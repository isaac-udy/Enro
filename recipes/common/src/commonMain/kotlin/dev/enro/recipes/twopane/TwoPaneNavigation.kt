/**
 * Enro Recipe: Two-Pane Scene
 *
 * Shows a custom `NavigationSceneStrategy` that renders the top two
 * backstack entries side by side. A destination sets `twoPane()` in metadata.
 * When the top two entries both have `twoPane()` and the window is at least
 * 600dp wide, the strategy renders both entries, with the lower entry on the
 * left. In every other case `SinglePaneSceneStrategy` renders the top entry.
 *
 * `TwoPaneScene.kt` declares `TwoPaneSceneStrategy` and the `twoPane()`
 * metadata function.
 *
 * Unlike the list-detail recipes, the two panes have no fixed roles: the
 * strategy applies to any two adjacent `twoPane()` destinations.
 *
 * Nav3 equivalent: the two-pane scene recipe,
 * https://developer.android.com/guide/navigation/navigation-3/recipes/scenes-twopane
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

@Serializable
object TwoPaneRecipe : NavigationKey

@Serializable
data object TwoPaneHome : NavigationKey

@Serializable
data class TwoPaneProduct(val id: Int) : NavigationKey

@Serializable
data object TwoPaneProfile : NavigationKey

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
        // region docs:scenes/display
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
        // endregion
    }
}

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
                "On a wide screen, opening another product shows the new " +
                    "product in the right pane and this product in the left " +
                    "pane. On a narrow screen, the new product covers this product.",
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
    // This destination has no twoPane() metadata. While this destination is
    // on top, TwoPaneSceneStrategy returns null and SinglePaneSceneStrategy
    // renders this destination full-screen at every window width.
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
