/**
 * Enro Recipes: Material List-Detail and Material Supporting Pane
 *
 * Two recipes that use the Material 3 Adaptive scaffolds as Enro scenes. The
 * scene strategies are in `MaterialAdaptiveScenes.kt`.
 *
 * In both, destinations are ordinary Enro destinations with metadata that
 * says which pane they belong in, and navigation is ordinary `open` and
 * `close`. On a wide window the strategy shows two destinations side by
 * side; on a narrow window it steps aside and the same backstack is shown
 * one screen at a time.
 *
 * Use these when you want Material's pane sizing, spacing and animation.
 * The List-Detail (Scene) recipe lays out the panes by hand instead.
 *
 * Nav3 equivalents: the Material List-Detail and Material Supporting Pane
 * recipes.
 */
package dev.enro.recipes.material

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.closeAndReplaceWith
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.recipes.listdetail.detailPane
import dev.enro.recipes.listdetail.listPane
import dev.enro.ui.LocalNavigationContainer
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.NavigationSceneStrategy
import dev.enro.ui.navigationDestination
import dev.enro.ui.rememberNavigationContainer
import dev.enro.ui.scenes.DialogSceneStrategy
import dev.enro.ui.scenes.DirectOverlaySceneStrategy
import dev.enro.ui.scenes.SinglePaneSceneStrategy
import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────
// Material List-Detail
// ─────────────────────────────────────────────────────────────────────

@Serializable
object MaterialListDetailRecipe : NavigationKey

@Serializable
data object MailList : NavigationKey

@Serializable
data class MailDetail(val subject: String) : NavigationKey

private val mailSubjects = listOf("Invoice", "Meeting notes", "Welcome")

@Composable
@NavigationDestination(MaterialListDetailRecipe::class)
fun MaterialListDetailRecipeScreen() {
    val navigation = navigationHandle<MaterialListDetailRecipe>()
    RecipeScaffold(
        title = "Material List-Detail",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(MailList.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
            // The Material strategy goes before the single-pane fallback.
            sceneStrategy = NavigationSceneStrategy.from(
                DialogSceneStrategy(),
                DirectOverlaySceneStrategy(),
                MaterialListDetailSceneStrategy(),
                SinglePaneSceneStrategy(),
            ),
        )
    }
}

@NavigationDestination(MailList::class)
val mailListDestination: NavigationDestinationProvider<MailList> = navigationDestination(
    metadata = {
        listPane(
            detailPlaceholder = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select a message")
                }
            },
        )
    },
) {
    val container = LocalNavigationContainer.current
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Inbox", style = MaterialTheme.typography.titleLarge)
        mailSubjects.forEach { subject ->
            Button(onClick = {
                // Keep one detail on the stack: selecting another message
                // replaces the open one instead of stacking on top of it.
                val openDetail = container.backstack.lastOrNull()?.takeIf { it.key is MailDetail }
                if (openDetail == null) {
                    navigation.open(MailDetail(subject))
                } else {
                    container.updateBackstack { backstackOf(MailList.asInstance(), MailDetail(subject).asInstance()) }
                }
            }) {
                Text(subject)
            }
        }
    }
}

@NavigationDestination(MailDetail::class)
val mailDetailDestination: NavigationDestinationProvider<MailDetail> = navigationDestination(
    metadata = { detailPane() },
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(navigation.key.subject, style = MaterialTheme.typography.titleLarge)
        Text("Message body for ${navigation.key.subject}.")
        Button(onClick = { navigation.close() }) { Text("Close message") }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Material Supporting Pane
// ─────────────────────────────────────────────────────────────────────

@Serializable
object MaterialSupportingPaneRecipe : NavigationKey

@Serializable
data object ArticleMain : NavigationKey

@Serializable
data class ArticleRelated(val topic: String) : NavigationKey

@Composable
@NavigationDestination(MaterialSupportingPaneRecipe::class)
fun MaterialSupportingPaneRecipeScreen() {
    val navigation = navigationHandle<MaterialSupportingPaneRecipe>()
    RecipeScaffold(
        title = "Material Supporting Pane",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(ArticleMain.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
            sceneStrategy = NavigationSceneStrategy.from(
                DialogSceneStrategy(),
                DirectOverlaySceneStrategy(),
                MaterialSupportingPaneSceneStrategy(),
                SinglePaneSceneStrategy(),
            ),
        )
    }
}

@Composable
@NavigationDestination(ArticleMain::class)
fun ArticleMainDestination() {
    val navigation = navigationHandle<ArticleMain>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Article", style = MaterialTheme.typography.titleLarge)
        Text("The main content. Related content opens beside it on a wide window.")
        Button(onClick = { navigation.open(ArticleRelated("Navigation")) }) {
            Text("Show related: Navigation")
        }
    }
}

@NavigationDestination(ArticleRelated::class)
val articleRelatedDestination: NavigationDestinationProvider<ArticleRelated> = navigationDestination(
    metadata = { supportingPane() },
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Related: ${navigation.key.topic}", style = MaterialTheme.typography.titleLarge)
        // Replaces this supporting destination with another one.
        Button(onClick = { navigation.closeAndReplaceWith(ArticleRelated("Animations")) }) {
            Text("Show related: Animations")
        }
        Button(onClick = { navigation.close() }) { Text("Hide related") }
    }
}
