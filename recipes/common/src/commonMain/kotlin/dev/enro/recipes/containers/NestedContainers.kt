/**
 * Enro Recipe: Nested Containers
 *
 * Demonstrates how `navigation.open(key)` picks a container when a screen
 * hosts more than one.
 *
 * This recipe's host screen has two containers side by side. Each has a
 * filter that says which keys it accepts:
 *  - the "notes" container accepts `Note` keys
 *  - the "tasks" container accepts `Task` keys
 *
 * When a destination calls `open`, Enro looks for a container whose filter
 * accepts the key. It starts with the container the caller is in and its
 * siblings, then walks up through the parent containers. So:
 *  - opening a `Note` from the tasks pane lands in the notes pane
 *  - opening a `Task` from the notes pane lands in the tasks pane
 *  - opening `ContainersHelp`, which neither pane accepts, goes up to the
 *    recipe's own container and covers both panes
 *
 * A container with `acceptNone()` never receives a key from `open`; its
 * backstack only changes when you update it directly.
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
        // The recipe's own container. It accepts every key, so it receives
        // any key the two panes inside ContainersHost do not accept.
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
    val notes = rememberNavigationContainer(
        key = NavigationContainer.Key("notes"),
        backstack = backstackOf(Note(1).asInstance()),
        filter = accept { key<Note>() },
        // Keep the first note: Back on it is not handled by this container.
        emptyBehavior = EmptyBehavior.preventEmpty(),
    )
    val tasks = rememberNavigationContainer(
        key = NavigationContainer.Key("tasks"),
        backstack = backstackOf(Task(1).asInstance()),
        filter = accept { key<Task>() },
        emptyBehavior = EmptyBehavior.preventEmpty(),
    )
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
        // Accepted by this pane's container.
        Button(onClick = { navigation.open(Note(number + 1)) }) {
            Text("Open note ${number + 1}")
        }
        // Not accepted here; the tasks pane accepts it.
        Button(onClick = { navigation.open(Task(number * 10)) }) {
            Text("Open task ${number * 10} in the other pane")
        }
        // Accepted by neither pane; opens in the recipe's container.
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
