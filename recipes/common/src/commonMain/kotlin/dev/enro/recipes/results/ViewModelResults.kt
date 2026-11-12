/**
 * Enro Recipe: Results in a ViewModel
 *
 * Demonstrates requesting and receiving a navigation result from a ViewModel
 * instead of from a Composable.
 *
 * `registerForNavigationResult` is available on `ViewModel` as a property
 * delegate. The ViewModel opens the result-bearing key through the channel
 * and handles `onCompleted` / `onClosed` itself, so the screen only renders
 * state and forwards clicks.
 *
 * The ViewModel must be created with `createEnroViewModel { }` so that
 * `by navigationHandle()` and the result channel can find the destination's
 * NavigationHandle.
 *
 * For the Composable form, see the Returning Results recipe.
 */
package dev.enro.recipes.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.recipes.RecipeScaffold
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import dev.enro.viewmodel.createEnroViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

@Serializable
object ViewModelResultsRecipe : NavigationKey

@Serializable
data object ThemeSettings : NavigationKey

@Serializable
data class PickAccentColour(val current: String) : NavigationKey.WithResult<String>

class ThemeSettingsViewModel : ViewModel() {
    private val _accentColour = MutableStateFlow("Blue")
    val accentColour: StateFlow<String> = _accentColour.asStateFlow()

    private val _status = MutableStateFlow("No colour picked yet")
    val status: StateFlow<String> = _status.asStateFlow()

    // The result channel. It survives configuration changes with the ViewModel.
    private val pickAccentColour by registerForNavigationResult<String>(
        onClosed = { _status.value = "Picker closed without a choice" },
        onCompleted = { colour ->
            _accentColour.value = colour
            _status.value = "Picked $colour"
        },
    )

    fun onChangeColourClicked() {
        pickAccentColour.open(PickAccentColour(current = _accentColour.value))
    }
}

@Composable
@NavigationDestination(ViewModelResultsRecipe::class)
fun ViewModelResultsRecipeScreen() {
    val navigation = navigationHandle<ViewModelResultsRecipe>()
    RecipeScaffold(
        title = "Results in a ViewModel",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(ThemeSettings.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(ThemeSettings::class)
fun ThemeSettingsDestination() {
    val viewModel = viewModel<ThemeSettingsViewModel> {
        createEnroViewModel { ThemeSettingsViewModel() }
    }
    val accentColour by viewModel.accentColour.collectAsState()
    val status by viewModel.status.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Theme settings", style = MaterialTheme.typography.titleLarge)
        Text("Accent colour: $accentColour")
        Text(status, style = MaterialTheme.typography.bodySmall)
        Button(onClick = { viewModel.onChangeColourClicked() }) {
            Text("Change colour")
        }
    }
}

@Composable
@NavigationDestination(PickAccentColour::class)
fun PickAccentColourDestination() {
    val navigation = navigationHandle<PickAccentColour>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Pick a colour", style = MaterialTheme.typography.titleLarge)
        Text("Current: ${navigation.key.current}")
        listOf("Blue", "Green", "Orange").forEach { colour ->
            Button(onClick = { navigation.complete(colour) }) { Text(colour) }
        }
        Button(onClick = { navigation.close() }) { Text("Cancel") }
    }
}
