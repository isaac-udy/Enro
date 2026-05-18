/**
 * Enro Recipe: List-Detail Navigation (scene-strategy)
 *
 * Demonstrates a list-detail adaptive layout using a single
 * `NavigationContainer` and a custom `NavigationSceneStrategy` that
 * inspects destination metadata to decide which pane each destination
 * belongs in.
 *
 * Modelled on the Nav3 Material list-detail recipe:
 * https://developer.android.com/guide/navigation/navigation-3/recipes/material-listdetail
 *
 * The strategy itself lives in [ListDetailScene.kt] alongside the
 * `listPane(detailPlaceholder = { ... })` and `detailPane()` metadata
 * helpers. When the window is at least 600dp wide and the backstack
 * contains a destination tagged with `listPane()`, the strategy
 * renders the list on the left and the topmost `detailPane()`
 * destination (or the placeholder) on the right. On narrower windows
 * the default single-pane strategy takes over.
 *
 * In a real app, prefer Material 3 Adaptive's `WindowSizeClass` APIs
 * over the hard-coded 600dp threshold used here.
 */
package dev.enro.recipes.listdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.NavigationSceneStrategy
import dev.enro.ui.navigationDestination
import dev.enro.ui.rememberNavigationContainer
import dev.enro.ui.scenes.DialogSceneStrategy
import dev.enro.ui.scenes.DirectOverlaySceneStrategy
import dev.enro.ui.scenes.SinglePaneSceneStrategy
import kotlinx.serialization.Serializable

// -- Recipe root --

@Serializable
object ListDetailRecipe : NavigationKey

// -- Demo destination keys --

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

// ─────────────────────────────────────────────────────────────────────
// Recipe root
// ─────────────────────────────────────────────────────────────────────

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
            sceneStrategy = NavigationSceneStrategy.from(
                DialogSceneStrategy(),
                DirectOverlaySceneStrategy(),
                ListDetailSceneStrategy(),
                SinglePaneSceneStrategy(),
            ),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────
// Destinations
// ─────────────────────────────────────────────────────────────────────

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
    // No pane metadata — when this destination is on top, the scene
    // strategy returns null and the single-pane strategy takes over,
    // rendering this destination full-screen even on wide windows.
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
