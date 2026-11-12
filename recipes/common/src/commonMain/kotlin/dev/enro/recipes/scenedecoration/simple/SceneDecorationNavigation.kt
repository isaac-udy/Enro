/**
 * Enro Recipe: Scene Decoration
 *
 * Shows a `SceneDecoratorStrategy`, which wraps the scene that the
 * `NavigationSceneStrategy` selects. [AdaptiveNavigationSceneDecorator] adds
 * a sidebar on a wide window and a Material 3 bottom navigation bar on a
 * narrow window. The section buttons replace the backstack through
 * `LocalNavigationContainer`.
 *
 * The Tab Navigation recipe builds a similar layout with one
 * `NavigationContainer` per tab. This recipe has one container and one
 * backstack.
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

@Serializable
object SceneDecorationRecipe : NavigationKey

@Serializable
data object HomeSection : NavigationKey

@Serializable
data object SearchSection : NavigationKey

@Serializable
data object FavoritesSection : NavigationKey

@Serializable
data object ProfileSection : NavigationKey

@Serializable
data class HomeDetail(val itemId: String) : NavigationKey

private val sections = listOf(
    NavigationSection(HomeSection, "Home", Icons.Filled.Home),
    NavigationSection(SearchSection, "Search", Icons.Filled.Search),
    NavigationSection(FavoritesSection, "Favorites", Icons.Filled.Star),
    NavigationSection(ProfileSection, "Profile", Icons.Filled.Person),
)

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
        // region docs:scenes/decorator-display
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
        // endregion
    }
}

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
        body = "The navigation bar does not animate when the section changes. The " +
            "decorator draws the navigation bar as a shared element with a constant key.",
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
