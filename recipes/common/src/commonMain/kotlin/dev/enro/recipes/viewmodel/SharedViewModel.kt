/**
 * Enro Recipe: Shared State (formerly Shared ViewModel)
 *
 * Demonstrates how to share state between parent and child destinations using
 * NavigationKey + Results, and via a ViewModel that exposes state to children
 * through results.
 */
package dev.enro.recipes.viewmodel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
object SharedViewModelRecipe : NavigationKey

@Serializable
data object ParentScreen : NavigationKey

@Serializable
data class ChildScreen(val currentData: String) : NavigationKey.WithResult<String>

@Composable
@NavigationDestination(SharedViewModelRecipe::class)
fun SharedViewModelRecipeScreen() {
    val navigation = navigationHandle<SharedViewModelRecipe>()
    RecipeScaffold(
        title = "Shared State",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(ParentScreen.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

class ParentViewModel : ViewModel() {
    private val _sharedData = MutableStateFlow("Initial shared data")
    val sharedData: StateFlow<String> = _sharedData.asStateFlow()

    fun updateData(newData: String) {
        _sharedData.value = newData
    }
}

@Composable
@NavigationDestination(ParentScreen::class)
fun ParentScreenDestination() {
    val viewModel = viewModel<ParentViewModel> { createEnroViewModel { ParentViewModel() } }
    val data by viewModel.sharedData.collectAsState()

    val openChild = registerForNavigationResult<String> { updatedData ->
        viewModel.updateData(updatedData)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Parent - Shared data:")
        Text(data)
        Button(onClick = { openChild.open(ChildScreen(data)) }) {
            Text("Open Child")
        }
    }
}

@Composable
@NavigationDestination(ChildScreen::class)
fun ChildScreenDestination() {
    val navigation = navigationHandle<ChildScreen>()
    var editedData by rememberSaveable { mutableStateOf(navigation.key.currentData) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Child - Editing shared data")
        TextField(
            value = editedData,
            onValueChange = { editedData = it },
        )
        Button(onClick = { navigation.complete(editedData) }) {
            Text("Save & Return")
        }
        Button(onClick = { navigation.close() }) {
            Text("Cancel")
        }
    }
}
