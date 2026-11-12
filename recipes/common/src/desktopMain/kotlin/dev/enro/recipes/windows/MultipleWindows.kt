/**
 * Enro Recipe: Multiple Windows (Desktop)
 *
 * Demonstrates a destination that opens in its own desktop window.
 *
 * `rootWindowDestination` binds a key to a window. Opening the key with
 * `navigation.open(key)` opens the window; closing it, from the window's
 * close button or with `navigation.close()`, closes the window. The window
 * has its own content, which can host a container and navigate on its own.
 *
 * A window destination can return a result like any other destination: the
 * note window below completes with the text that was typed in it.
 *
 * To open an existing common destination in a window on desktop only,
 * annotate a `rootWindowDestination` with
 * `@NavigationDestination.PlatformOverride(Key::class)`. The Loan
 * Application sample does this for its create flow.
 */
package dev.enro.recipes.windows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.platform.desktop.RootWindow
import dev.enro.recipes.RecipeScaffold
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.destinations.rootWindowDestination
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object MultipleWindowsRecipe : NavigationKey

@Serializable
data object WindowsMenu : NavigationKey

/** Opens in its own window, and returns the note that was written. */
@Serializable
data class NoteWindow(val number: Int) : NavigationKey.WithResult<String>

@Composable
@NavigationDestination(MultipleWindowsRecipe::class)
fun MultipleWindowsRecipeScreen() {
    val navigation = navigationHandle<MultipleWindowsRecipe>()
    RecipeScaffold(
        title = "Multiple Windows",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(WindowsMenu.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(WindowsMenu::class)
fun WindowsMenuDestination() {
    val notes = remember { mutableStateListOf<String>() }
    var opened by remember { mutableStateOf(0) }
    // Opening this key opens a window; its result arrives here when the
    // window completes.
    val newNote = registerForNavigationResult<String> { note -> notes.add(note) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Notes", style = MaterialTheme.typography.titleLarge)
        Button(onClick = {
            opened++
            newNote.open(NoteWindow(number = opened))
        }) {
            Text("Write a note in a new window")
        }
        if (notes.isEmpty()) {
            Text("No notes yet", style = MaterialTheme.typography.bodySmall)
        }
        notes.forEach { Text("- $it") }
    }
}

@NavigationDestination(NoteWindow::class)
val noteWindowDestination = rootWindowDestination<NoteWindow>(
    windowConfiguration = {
        RootWindow.WindowConfiguration(
            title = "New note",
            // The window's close button closes the destination without a result.
            onCloseRequest = { navigation.close() },
        )
    },
) {
    var text by remember { mutableStateOf("") }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Note ${navigation.key.number}", style = MaterialTheme.typography.titleLarge)
                TextField(value = text, onValueChange = { text = it })
                // Completing closes the window and delivers the result.
                Button(onClick = { navigation.complete(text) }) { Text("Save") }
                Button(onClick = { navigation.close() }) { Text("Discard") }
            }
        }
    }
}
