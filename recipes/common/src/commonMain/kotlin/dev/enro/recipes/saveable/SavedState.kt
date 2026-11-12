/**
 * Enro Recipe: Saved State
 *
 * Shows four places where a destination can store state, and when each place
 * keeps the state. The screen has four counters:
 *
 * | Stored in                         | Covered by another screen | Rotation | Process death |
 * |-----------------------------------|---------------------------|----------|---------------|
 * | `remember`                        | lost                      | lost     | lost          |
 * | `rememberSaveable`                | kept                      | kept     | kept          |
 * | a ViewModel property              | kept                      | kept     | lost          |
 * | the ViewModel's `SavedStateHandle`| kept                      | kept     | kept          |
 *
 * A destination leaves the composition when another destination opens on
 * top. While the covered destination is on the backstack, Enro saves the
 * `rememberSaveable` state of the destination and keeps the ViewModel. Enro
 * restores both when the destination is shown again. `remember` state is
 * lost.
 *
 * `rememberNavigationContainer` saves the backstack with `rememberSaveable`,
 * so after process death the app shows the same screens.
 *
 * To test process death on Android, turn on "Don't keep activities" in
 * Developer Options, send the app to the background and open the app again.
 *
 * Nav3 equivalent: the Saveable back stack recipe.
 */
package dev.enro.recipes.saveable

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import dev.enro.viewmodel.createEnroViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable

@Serializable
object SavedStateRecipe : NavigationKey

@Serializable
data object Counters : NavigationKey

@Serializable
data object CoveringScreen : NavigationKey

// region docs:saved-state/view-model
class CountersViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    // A ViewModel property is lost on process death.
    private val _inMemory = MutableStateFlow(0)
    val inMemory: StateFlow<Int> = _inMemory.asStateFlow()

    // A SavedStateHandle value is restored after process death.
    val saved: StateFlow<Int> = savedStateHandle.getStateFlow("saved", 0)

    fun increment() {
        _inMemory.update { it + 1 }
        savedStateHandle["saved"] = saved.value + 1
    }
}
// endregion

@Composable
@NavigationDestination(SavedStateRecipe::class)
fun SavedStateRecipeScreen() {
    val navigation = navigationHandle<SavedStateRecipe>()
    RecipeScaffold(
        title = "Saved State",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(Counters.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(Counters::class)
fun CountersDestination() {
    val navigation = navigationHandle<Counters>()
    // region docs:saved-state/destination
    var remembered by remember { mutableIntStateOf(0) }
    var saveable by rememberSaveable { mutableIntStateOf(0) }
    // createSavedStateHandle() creates a SavedStateHandle scoped to this destination.
    val viewModel = viewModel<CountersViewModel> {
        createEnroViewModel { CountersViewModel(createSavedStateHandle()) }
    }
    // endregion
    val inMemory by viewModel.inMemory.collectAsState()
    val saved by viewModel.saved.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Counters", style = MaterialTheme.typography.titleLarge)
        Text("remember: $remembered")
        Text("rememberSaveable: $saveable")
        Text("ViewModel property: $inMemory")
        Text("SavedStateHandle: $saved")
        Button(onClick = {
            remembered++
            saveable++
            viewModel.increment()
        }) {
            Text("Add one to each")
        }
        Button(onClick = { navigation.open(CoveringScreen) }) {
            Text("Open a screen on top")
        }
    }
}

@Composable
@NavigationDestination(CoveringScreen::class)
fun CoveringScreenDestination() {
    val navigation = navigationHandle<CoveringScreen>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Covering screen", style = MaterialTheme.typography.titleLarge)
        Text("The counters screen is on the backstack but not composed.")
        Button(onClick = { navigation.close() }) { Text("Back to counters") }
    }
}
