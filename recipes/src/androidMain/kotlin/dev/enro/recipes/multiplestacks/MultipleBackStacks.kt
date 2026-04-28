/**
 * Enro Recipe: Multiple Back Stacks
 *
 * Demonstrates Enro's NavigationContainerGroup managing multiple independent backstacks.
 *
 * NavigationContainerGroup is provided by enro-compat and is currently Android-only,
 * so this recipe lives in androidMain.
 */
package dev.enro.recipes.multiplestacks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationContainer
import dev.enro.NavigationKey
import dev.enro.acceptNone
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.core.compose.container.rememberNavigationContainerGroup
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.EmptyBehavior
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
data object FeedRoot : NavigationKey

@Serializable
data object MessagesRoot : NavigationKey

@Serializable
data object SettingsRoot : NavigationKey

@Serializable
data class FeedPost(val postId: String) : NavigationKey

@Serializable
data class MessageThread(val threadId: String) : NavigationKey

@Serializable
data class SettingsDetail(val section: String) : NavigationKey

@Composable
@NavigationDestination(MultipleBackStacksRecipe::class)
fun MultipleBackStacksRecipeScreen() {
    val navigation = navigationHandle<MultipleBackStacksRecipe>()
    RecipeScaffold(
        title = "Multiple Back Stacks",
        navigation = navigation,
    ) { modifier ->
        val feedContainer = rememberNavigationContainer(
            key = NavigationContainer.Key("feed"),
            backstack = backstackOf(FeedRoot.asInstance()),
            filter = acceptNone(),
            emptyBehavior = EmptyBehavior.preventEmpty(),
        )

        val messagesContainer = rememberNavigationContainer(
            key = NavigationContainer.Key("messages"),
            backstack = backstackOf(MessagesRoot.asInstance()),
            filter = acceptNone(),
            emptyBehavior = EmptyBehavior.preventEmpty(),
        )

        val settingsContainer = rememberNavigationContainer(
            key = NavigationContainer.Key("settings"),
            backstack = backstackOf(SettingsRoot.asInstance()),
            filter = acceptNone(),
            emptyBehavior = EmptyBehavior.preventEmpty(),
        )

        val group = rememberNavigationContainerGroup(
            feedContainer,
            messagesContainer,
            settingsContainer,
        )

        Column(modifier = modifier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                NavigationDisplay(state = group.activeContainer)
            }

            NavigationBar {
                val tabs = listOf(
                    "Feed" to feedContainer,
                    "Messages" to messagesContainer,
                    "Settings" to settingsContainer,
                )
                tabs.forEach { (label, container) ->
                    NavigationBarItem(
                        selected = container == group.activeContainer,
                        onClick = { group.setActive(container) },
                        icon = {},
                        label = { Text(label) },
                    )
                }
            }
        }
    }
}

@Composable
@NavigationDestination(FeedRoot::class)
fun FeedRootDestination() {
    val navigation = navigationHandle<FeedRoot>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Feed")
        Button(onClick = { navigation.open(FeedPost("post-1")) }) {
            Text("View Post 1")
        }
        Button(onClick = { navigation.open(FeedPost("post-2")) }) {
            Text("View Post 2")
        }
    }
}

@Composable
@NavigationDestination(MessagesRoot::class)
fun MessagesRootDestination() {
    val navigation = navigationHandle<MessagesRoot>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Messages")
        Button(onClick = { navigation.open(MessageThread("thread-1")) }) {
            Text("Thread 1")
        }
    }
}

@Composable
@NavigationDestination(SettingsRoot::class)
fun SettingsRootDestination() {
    val navigation = navigationHandle<SettingsRoot>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Settings")
        Button(onClick = { navigation.open(SettingsDetail("account")) }) {
            Text("Account Settings")
        }
        Button(onClick = { navigation.open(SettingsDetail("notifications")) }) {
            Text("Notification Settings")
        }
    }
}

@Composable
@NavigationDestination(FeedPost::class)
fun FeedPostDestination() {
    val navigation = navigationHandle<FeedPost>()
    var likeCount by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Post: ${navigation.key.postId}")
        Text("Likes: $likeCount")
        Button(onClick = { likeCount++ }) {
            Text("Like")
        }
        Button(onClick = { navigation.close() }) {
            Text("Back")
        }
    }
}

@Composable
@NavigationDestination(MessageThread::class)
fun MessageThreadDestination() {
    val navigation = navigationHandle<MessageThread>()
    var draftText by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Thread: ${navigation.key.threadId}")
        TextField(
            value = draftText,
            onValueChange = { draftText = it },
            label = { Text("Type a message...") },
        )
        Button(onClick = { navigation.close() }) {
            Text("Back")
        }
    }
}

@Composable
@NavigationDestination(SettingsDetail::class)
fun SettingsDetailDestination() {
    val navigation = navigationHandle<SettingsDetail>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Settings: ${navigation.key.section}")
        Button(onClick = { navigation.close() }) {
            Text("Back")
        }
    }
}
