/**
 * Enro Recipe: Multiple Back Stacks
 *
 * Nav3 equivalent: "Multiple Back Stacks" recipe
 * https://nicbell.github.io/nav3/recipes/multiple-back-stacks
 *
 * Demonstrates how Enro's NavigationContainerGroup naturally handles multiple backstacks,
 * compared to Nav3's manual approach.
 *
 * Key differences from Nav3:
 * - Nav3 requires you to manually manage multiple backstack lists, save/restore them with
 *   rememberNavBackStack, and swap which NavDisplay is rendered.
 * - Enro's NavigationContainerGroup handles all of this automatically:
 *   - Each container maintains its own backstack
 *   - Switching containers preserves the previous container's backstack and saved state
 *   - The active container is automatically saved/restored across config changes
 *   - Each container has its own per-destination saved state (rememberSaveable)
 * - The key insight: Enro's containers are persistent objects that exist even when not displayed.
 *   Nav3's backstacks are just lists that you need to manage yourself.
 */
package dev.enro.recipes.multiplestacks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import dev.enro.ui.EmptyBehavior
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

// -- Navigation Keys --

@Serializable
data object MultiStackHost : NavigationKey

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

// -- Host --

@Composable
@NavigationDestination(MultiStackHost::class)
fun MultiStackHostDestination() {
    // Each container has an independent backstack.
    // Nav3 equivalent:
    //   val feedBackStack = rememberNavBackStack(FeedRoot)
    //   val messagesBackStack = rememberNavBackStack(MessagesRoot)
    //   val settingsBackStack = rememberNavBackStack(SettingsRoot)
    //
    // The critical difference: Nav3 backstacks are just List<Any> that you manage.
    // Enro containers are full navigation contexts with their own saved state holders.

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

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            // Only the active container's UI is displayed.
            // But all containers maintain their backstack and state.
            //
            // Nav3 equivalent: when(selectedTab) { 0 -> NavDisplay(feedBackStack); ... }
            // With Enro, you just display the active container from the group.
            NavigationDisplay(state = group.activeContainer)
        }

        NavigationBar {
            val tabs = listOf("Feed" to feedContainer, "Messages" to messagesContainer, "Settings" to settingsContainer)
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

// -- Tab Root Destinations --

@Composable
@NavigationDestination(FeedRoot::class)
fun FeedRootDestination() {
    val navigation = navigationHandle<FeedRoot>()
    Column {
        Text("Feed")
        // Navigation within a tab pushes onto that tab's backstack.
        // Switching to another tab and back will show this tab's full backstack.
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
    Column {
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
    Column {
        Text("Settings")
        Button(onClick = { navigation.open(SettingsDetail("account")) }) {
            Text("Account Settings")
        }
        Button(onClick = { navigation.open(SettingsDetail("notifications")) }) {
            Text("Notification Settings")
        }
    }
}

// -- Detail Destinations --
// These demonstrate state preservation across tab switches.

@Composable
@NavigationDestination(FeedPost::class)
fun FeedPostDestination() {
    val navigation = navigationHandle<FeedPost>()
    // This counter survives tab switches because the container preserves saved state.
    var likeCount by rememberSaveable { mutableIntStateOf(0) }

    Column {
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
    // Draft text is preserved when switching tabs and coming back.
    var draftText by rememberSaveable { mutableStateOf("") }

    Column {
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
    Column {
        Text("Settings: ${navigation.key.section}")
        Button(onClick = { navigation.close() }) {
            Text("Back")
        }
    }
}
