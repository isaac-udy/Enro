/**
 * Enro Recipe: Returning Results
 *
 * Nav3 equivalent: "Returning Results" recipe
 * https://nicbell.github.io/nav3/recipes/results
 *
 * Demonstrates how Enro handles typed results between screens, compared to Nav3's
 * manual approach using Channel/StateMap.
 *
 * Key differences from Nav3:
 * - Nav3 requires manual result passing, typically through shared state, Channels,
 *   or by inspecting the backstack after a screen is popped.
 * - Enro has first-class result support via NavigationKey.WithResult<T> and
 *   registerForNavigationResult. Results are type-safe and delivered automatically.
 * - Results survive configuration changes and process death.
 * - registerForNavigationResult works in both Composables and ViewModels.
 * - The result channel handles the complete lifecycle: opening the destination,
 *   waiting for the result, and delivering it back to the caller.
 */
package dev.enro.recipes.results

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.result.NavigationResultChannel
import dev.enro.result.registerForNavigationResult
import dev.enro.viewmodel.createEnroViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

// -- Navigation Keys --
// NavigationKey.WithResult<T> declares that a destination returns a typed result.
// Nav3 has no equivalent annotation -- results are ad-hoc.

@Serializable
data object ResultsHome : NavigationKey

@Serializable
data class EnterText(val prompt: String) : NavigationKey.WithResult<String>

@Serializable
data class SelectColor(val colors: List<String>) : NavigationKey.WithResult<String>

@Serializable
data class ConfirmAction(val message: String) : NavigationKey.WithResult<Boolean>

// ============================================================
// Pattern 1: Composable result handling (event-based)
// ============================================================

@Composable
@NavigationDestination(ResultsHome::class)
fun ResultsHomeDestination() {
    val navigation = navigationHandle<ResultsHome>()

    var enteredText by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedColor by rememberSaveable { mutableStateOf<String?>(null) }
    var isConfirmed by rememberSaveable { mutableStateOf<Boolean?>(null) }

    // registerForNavigationResult creates a channel that:
    // 1. Opens the destination when you call channel.open(key)
    // 2. Delivers the result to the onCompleted callback
    // 3. Optionally handles when the destination is closed without a result (onClosed)
    //
    // Nav3 equivalent: You'd use a Channel<T> or shared mutable state.
    // Enro makes this type-safe and lifecycle-aware.

    val getTextResult = registerForNavigationResult<String>(
        onClosed = {
            // Called when the destination was closed without providing a result
            // (e.g., user pressed back). Nav3 has no built-in equivalent.
        },
        onCompleted = { result ->
            enteredText = result
        },
    )

    val getColorResult = registerForNavigationResult<String> { color ->
        selectedColor = color
    }

    val getConfirmation = registerForNavigationResult<Boolean> { confirmed ->
        isConfirmed = confirmed
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Results Demo")
        Text("Text: ${enteredText ?: "not entered"}")
        Text("Color: ${selectedColor ?: "not selected"}")
        Text("Confirmed: ${isConfirmed ?: "not answered"}")

        // open() on the result channel opens the destination AND registers for its result.
        // Nav3: backStack.add(EnterText("...")) then somehow observe for the result.
        Button(onClick = { getTextResult.open(EnterText("Enter your name")) }) {
            Text("Enter Text")
        }

        Button(onClick = { getColorResult.open(SelectColor(listOf("Red", "Green", "Blue"))) }) {
            Text("Select Color")
        }

        Button(onClick = { getConfirmation.open(ConfirmAction("Delete all items?")) }) {
            Text("Confirm Action")
        }
    }
}

// ============================================================
// Pattern 2: ViewModel result handling
// ============================================================

class ResultsViewModel : ViewModel() {
    private val navigation by navigationHandle<ResultsHome>()

    private val _lastResult = MutableStateFlow<String>("No result yet")
    val lastResult: StateFlow<String> = _lastResult.asStateFlow()

    // registerForNavigationResult in a ViewModel works the same way.
    // The result channel is tied to the ViewModel's lifecycle.
    val getTextResult: NavigationResultChannel<String> by registerForNavigationResult<String>(
        onClosed = {
            _lastResult.value = "Cancelled"
        },
        onCompleted = { result ->
            _lastResult.value = result
        },
    )

    fun requestText() {
        getTextResult.open(EnterText("Enter something from ViewModel"))
    }
}

// ============================================================
// Result-producing destinations
// ============================================================

@Composable
@NavigationDestination(EnterText::class)
fun EnterTextDestination() {
    val navigation = navigationHandle<EnterText>()
    var text by rememberSaveable { mutableStateOf("") }

    Column {
        Text(navigation.key.prompt)

        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                // complete() closes this destination AND delivers the result to the caller.
                // Nav3: You'd need to set a shared state/channel value then close the screen.
                navigation.complete(text)
            },
            enabled = text.isNotBlank(),
        ) {
            Text("Submit")
        }

        Button(onClick = {
            // close() without complete() triggers onClosed on the caller.
            navigation.close()
        }) {
            Text("Cancel")
        }
    }
}

@Composable
@NavigationDestination(SelectColor::class)
fun SelectColorDestination() {
    val navigation = navigationHandle<SelectColor>()

    Column {
        Text("Select a color:")
        navigation.key.colors.forEach { color ->
            Button(onClick = { navigation.complete(color) }) {
                Text(color)
            }
        }
        Button(onClick = { navigation.close() }) {
            Text("Cancel")
        }
    }
}

@Composable
@NavigationDestination(ConfirmAction::class)
fun ConfirmActionDestination() {
    val navigation = navigationHandle<ConfirmAction>()

    Column {
        Text(navigation.key.message)
        Button(onClick = { navigation.complete(true) }) {
            Text("Yes")
        }
        Button(onClick = { navigation.complete(false) }) {
            Text("No")
        }
    }
}
