/**
 * Enro Recipe: Nested Containers
 *
 * Shows how `navigation.open(key)` selects a container when a screen hosts
 * more than one container.
 *
 * The host screen of the recipe has two containers side by side. The filter
 * of each container states which keys the container accepts:
 *  - the "notes" container accepts `Note` keys
 *  - the "tasks" container accepts `Task` keys
 *
 * When a destination calls `open`, Enro searches for a container whose filter
 * accepts the key. The search covers the containers nested in the caller's
 * container, then the caller's container and the sibling containers, then
 * each parent container in turn:
 *  - a `Note` opened from the tasks pane opens in the notes pane
 *  - a `Task` opened from the notes pane opens in the tasks pane
 *  - `ContainersHelp`, which neither pane accepts, opens in the recipe's
 *    container and covers both panes
 *
 * A container with `acceptNone()` accepts no key from `open`. Only a direct
 * backstack update changes the backstack of that container.
 */
package dev.enro.recipes.containers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationContainer
import dev.enro.NavigationKey
import dev.enro.accept
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.EmptyBehavior
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object NestedContainersRecipe : NavigationKey

@Serializable
data object ContainersHost : NavigationKey

@Serializable
data class Note(val number: Int) : NavigationKey

@Serializable
data class Task(val number: Int) : NavigationKey

@Serializable
data object ContainersHelp : NavigationKey

@Composable
@NavigationDestination(NestedContainersRecipe::class)
fun NestedContainersRecipeScreen() {
    val navigation = navigationHandle<NestedContainersRecipe>()
    RecipeScaffold(
        title = "Nested Containers",
        navigation = navigation,
    ) { modifier ->
        // This container has the default filter, which accepts every key. A key
        // that neither pane inside ContainersHost accepts opens in this container.
        val container = rememberNavigationContainer(
            backstack = backstackOf(ContainersHost.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(ContainersHost::class)
fun ContainersHostDestination() {
    // region docs:containers/filters
    val notes = rememberNavigationContainer(
        key = NavigationContainer.Key("notes"),
        backstack = backstackOf(Note(1).asInstance()),
        filter = accept { key<Note>() },
        // preventEmpty keeps the first note: this container does not handle Back on the first note.
        emptyBehavior = EmptyBehavior.preventEmpty(),
    )
    val tasks = rememberNavigationContainer(
        key = NavigationContainer.Key("tasks"),
        backstack = backstackOf(Task(1).asInstance()),
        filter = accept { key<Task>() },
        emptyBehavior = EmptyBehavior.preventEmpty(),
    )
    // endregion
    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            NavigationDisplay(state = notes)
        }
        VerticalDivider()
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            NavigationDisplay(state = tasks)
        }
    }
}

@Composable
@NavigationDestination(Note::class)
fun NoteDestination() {
    val navigation = navigationHandle<Note>()
    val number = navigation.key.number
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Note $number", style = MaterialTheme.typography.titleLarge)
        // The notes container accepts Note keys.
        Button(onClick = { navigation.open(Note(number + 1)) }) {
            Text("Open note ${number + 1}")
        }
        // The notes container rejects Task keys; the tasks container accepts Task keys.
        Button(onClick = { navigation.open(Task(number * 10)) }) {
            Text("Open task ${number * 10} in the other pane")
        }
        // Neither pane accepts ContainersHelp, so the key opens in the recipe's container.
        Button(onClick = { navigation.open(ContainersHelp) }) {
            Text("Open help over both panes")
        }
        if (number > 1) {
            Button(onClick = { navigation.close() }) { Text("Back") }
        }
    }
}

@Composable
@NavigationDestination(Task::class)
fun TaskDestination() {
    val navigation = navigationHandle<Task>()
    val number = navigation.key.number
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Task $number", style = MaterialTheme.typography.titleLarge)
        Button(onClick = { navigation.open(Task(number + 1)) }) {
            Text("Open task ${number + 1}")
        }
        Button(onClick = { navigation.open(Note(number * 10)) }) {
            Text("Open note ${number * 10} in the other pane")
        }
        if (number > 1) {
            Button(onClick = { navigation.close() }) { Text("Back") }
        }
    }
}

@Composable
@NavigationDestination(ContainersHelp::class)
fun ContainersHelpDestination() {
    val navigation = navigationHandle<ContainersHelp>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Help", style = MaterialTheme.typography.titleLarge)
        Text("Neither pane accepts this key, so it opened in the container that hosts both panes.")
        Button(onClick = { navigation.close() }) { Text("Back") }
    }
}
