/**
 * Enro Recipe: Results in a ViewModel
 *
 * Shows a ViewModel that opens a screen for a result and receives the result.
 *
 * `registerForNavigationResult` is a property delegate on `ViewModel`. The
 * ViewModel opens the `NavigationKey.WithResult` key through the result
 * channel, and the `onCompleted` and `onClosed` callbacks run in the
 * ViewModel. The screen renders ViewModel state and forwards clicks.
 *
 * The ViewModel constructor must run inside `createEnroViewModel { }`, which
 * provides the destination's NavigationHandle to `by navigationHandle()` and
 * to the result channel.
 *
 * The Returning Results recipe shows `registerForNavigationResult` in a
 * Composable.
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

    // The ViewModel owns the result channel, so a configuration change does
    // not recreate the result channel.
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
