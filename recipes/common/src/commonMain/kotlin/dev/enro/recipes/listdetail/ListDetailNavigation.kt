/**
 * Enro Recipe: List-Detail Navigation (scene-strategy)
 *
 * Shows an adaptive list-detail layout with one `NavigationContainer` and a
 * custom `NavigationSceneStrategy`. The strategy reads destination metadata
 * to select the pane for each destination.
 *
 * `ListDetailScene.kt` declares `ListDetailSceneStrategy` and the metadata
 * functions `listPane(detailPlaceholder = { ... })` and `detailPane()`. When
 * the window is at least 600dp wide and the backstack contains a `listPane()`
 * destination, the strategy renders the list on the left. The right pane
 * shows the top destination when the top destination has `detailPane()`, and
 * the placeholder when the list is the top destination. In every other case
 * `SinglePaneSceneStrategy` renders the top destination.
 *
 * The 600dp threshold is hard-coded. Material 3 Adaptive provides
 * `WindowSizeClass` for the same decision.
 *
 * Nav3 equivalent: the Material list-detail recipe,
 * https://developer.android.com/guide/navigation/navigation-3/recipes/material-listdetail
 */
package dev.enro.recipes.listdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
object ListDetailRecipe : NavigationKey

@Serializable
data object ConversationList : NavigationKey

@Serializable
data class ConversationDetail(val id: String) : NavigationKey

@Serializable
data object ConversationProfile : NavigationKey

private data class Conversation(val id: String, val title: String, val preview: String)

private val sampleConversations = listOf(
    Conversation("A", "Alex", "Hey, are you around tomorrow?"),
    Conversation("B", "Bea", "Sent the files over — let me know."),
    Conversation("C", "Cam", "👍"),
)

@Composable
@NavigationDestination(ListDetailRecipe::class)
fun ListDetailRecipeScreen() {
    val navigation = navigationHandle<ListDetailRecipe>()
    RecipeScaffold(
        title = "List-Detail (Scene)",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(ConversationList.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
            // NavigationSceneStrategy.from uses the first strategy that returns a
            // scene, so ListDetailSceneStrategy is listed before SinglePaneSceneStrategy.
            sceneStrategy = NavigationSceneStrategy.from(
                DialogSceneStrategy(),
                DirectOverlaySceneStrategy(),
                ListDetailSceneStrategy(),
                SinglePaneSceneStrategy(),
            ),
        )
    }
}

@NavigationDestination(ConversationList::class)
val conversationListDestination: NavigationDestinationProvider<ConversationList> =
    navigationDestination(
        metadata = {
            listPane(
                detailPlaceholder = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Pick a conversation to read it.",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                },
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(sampleConversations) { conversation ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navigation.open(ConversationDetail(conversation.id))
                        },
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(conversation.title, style = MaterialTheme.typography.titleMedium)
                        Text(conversation.preview, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

@NavigationDestination(ConversationDetail::class)
val conversationDetailDestination: NavigationDestinationProvider<ConversationDetail> =
    navigationDestination(
        metadata = { detailPane() },
    ) {
        val conversation = sampleConversations.find { it.id == navigation.key.id }
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (conversation != null) {
                Text(conversation.title, style = MaterialTheme.typography.headlineSmall)
                Text(conversation.preview, style = MaterialTheme.typography.bodyLarge)
            } else {
                Text("Conversation ${navigation.key.id} not found.")
            }

            Button(onClick = { navigation.open(ConversationProfile) }) {
                Text("View profile")
            }
            Button(onClick = { navigation.close() }) {
                Text("Back")
            }
        }
    }

@Composable
@NavigationDestination(ConversationProfile::class)
fun ConversationProfileDestination() {
    // This destination has no pane metadata. While this destination is on
    // top, ListDetailSceneStrategy returns null and SinglePaneSceneStrategy
    // renders this destination full-screen at every window width.
    val navigation = navigationHandle<ConversationProfile>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineSmall)
        Text(
            "This destination has no pane metadata, so the scene strategy " +
                "falls through and it renders full-screen.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = { navigation.close() }) {
            Text("Back")
        }
    }
}
