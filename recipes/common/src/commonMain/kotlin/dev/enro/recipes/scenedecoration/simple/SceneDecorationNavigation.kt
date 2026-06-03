/**
 * Enro Recipe: Scene Decoration
 *
 * Demonstrates `SceneDecoratorStrategy` — wrapping the scene produced by a
 * regular `NavigationSceneStrategy` chain with extra chrome that lives
 * *outside* the destination layer. This recipe's decorator switches between
 * a vertical sidebar (on wide windows) and a Material 3 bottom navigation
 * bar (on narrow windows), with the section buttons driving navigation
 * directly via `LocalNavigationContainer`.
 *
 * Contrast with the `Tab Navigation` recipe, which achieves a similar
 * visual result with multiple sibling `NavigationContainer`s. The
 * scene-decoration approach uses **one** container and one backstack —
 * the decorator just adds chrome.
 *
 * See [AdaptiveNavigationSceneDecorator] for the decorator implementation.
 */
package dev.enro.recipes.scenedecoration.simple

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.NavigationSceneStrategy
import dev.enro.ui.rememberNavigationContainer
import dev.enro.ui.scenes.DialogSceneStrategy
import dev.enro.ui.scenes.DirectOverlaySceneStrategy
import dev.enro.ui.scenes.SinglePaneSceneStrategy
import kotlinx.serialization.Serializable

// -- Recipe root --

@Serializable
object SceneDecorationRecipe : NavigationKey

// -- Section root destinations --

@Serializable
data object HomeSection : NavigationKey

@Serializable
data object SearchSection : NavigationKey

@Serializable
data object FavoritesSection : NavigationKey

@Serializable
data object ProfileSection : NavigationKey

// -- Inner destination (demonstrates push within a section) --

@Serializable
data class HomeDetail(val itemId: String) : NavigationKey

private val sections = listOf(
    NavigationSection(HomeSection, "Home", Icons.Filled.Home),
    NavigationSection(SearchSection, "Search", Icons.Filled.Search),
    NavigationSection(FavoritesSection, "Favorites", Icons.Filled.Star),
    NavigationSection(ProfileSection, "Profile", Icons.Filled.Person),
)

// ─────────────────────────────────────────────────────────────────────
// Recipe screen
// ─────────────────────────────────────────────────────────────────────

@Composable
@NavigationDestination(SceneDecorationRecipe::class)
fun SceneDecorationRecipeScreen() {
    val navigation = navigationHandle<SceneDecorationRecipe>()
    RecipeScaffold(
        title = "Scene Decoration",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(HomeSection.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
            sceneStrategy = remember {
                NavigationSceneStrategy.from(
                    DialogSceneStrategy(),
                    DirectOverlaySceneStrategy(),
                    SinglePaneSceneStrategy(),
                )
            },
            sceneDecoratorStrategies = listOf(
                remember { AdaptiveNavigationSceneDecorator(sections) },
            ),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────
// Section destinations
// ─────────────────────────────────────────────────────────────────────

@Composable
@NavigationDestination(HomeSection::class)
fun HomeSectionDestination() {
    val navigation = navigationHandle<HomeSection>()
    SectionContent(
        title = "Home",
        body = "On wide windows this section renders beside the sidebar. " +
            "On narrow windows the bottom navigation bar appears below.",
    ) {
        Button(onClick = { navigation.open(HomeDetail("item-1")) }) {
            Text("Open a detail screen")
        }
    }
}

@Composable
@NavigationDestination(SearchSection::class)
fun SearchSectionDestination() {
    SectionContent(
        title = "Search",
        body = "Tap a different section to see the backstack swap underneath the chrome.",
    )
}

@Composable
@NavigationDestination(FavoritesSection::class)
fun FavoritesSectionDestination() {
    SectionContent(
        title = "Favorites",
        body = "The chrome itself does not re-animate when you change sections — the " +
            "decorator returns a scene with a constant key so the AnimatedContent slot is reused.",
    )
}

@Composable
@NavigationDestination(ProfileSection::class)
fun ProfileSectionDestination() {
    SectionContent(
        title = "Profile",
        body = "Try resizing the window to flip between the sidebar and bottom-bar layouts.",
    )
}

@Composable
@NavigationDestination(HomeDetail::class)
fun HomeDetailDestination() {
    val navigation = navigationHandle<HomeDetail>()
    SectionContent(
        title = "Detail: ${navigation.key.itemId}",
        body = "A detail destination pushed on top of the Home section. The chrome stays " +
            "around it — the decorator wraps whatever the scene strategy resolves.",
    ) {
        Button(onClick = { navigation.close() }) { Text("Back") }
    }
}

@Composable
private fun SectionContent(
    title: String,
    body: String,
    content: @Composable () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(body, style = MaterialTheme.typography.bodyLarge)
        content()
    }
}
