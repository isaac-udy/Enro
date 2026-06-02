/**
 * Enro Recipe: Basic ViewModel
 *
 * Demonstrates using NavigationHandle in a ViewModel.
 */
package dev.enro.recipes.viewmodel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationHandle
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object BasicViewModelRecipe : NavigationKey

@Serializable
data class UserScreen(val userId: String) : NavigationKey

@Serializable
data class EditUserScreen(val userId: String) : NavigationKey

@Composable
@NavigationDestination(BasicViewModelRecipe::class)
fun BasicViewModelRecipeScreen() {
    val navigation = navigationHandle<BasicViewModelRecipe>()
    RecipeScaffold(
        title = "Basic ViewModel",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(UserScreen("user-1").asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

class UserViewModel : ViewModel() {
    private val navigation: NavigationHandle<UserScreen> by navigationHandle()

    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            delay(1000)
            _uiState.value = UserUiState.Success(
                userName = "User ${navigation.key.userId}",
                email = "${navigation.key.userId}@example.com",
            )
        }
    }

    fun onEditClicked() {
        navigation.open(EditUserScreen(navigation.key.userId))
    }

    fun onBackClicked() {
        navigation.close()
    }
}

sealed interface UserUiState {
    data object Loading : UserUiState
    data class Success(val userName: String, val email: String) : UserUiState
    data class Error(val message: String) : UserUiState
}

@Composable
@NavigationDestination(UserScreen::class)
fun UserScreenDestination() {
    val viewModel = viewModel<UserViewModel> { createEnroViewModel { UserViewModel() } }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (val state = uiState) {
            is UserUiState.Loading -> {
                CircularProgressIndicator()
            }
            is UserUiState.Success -> {
                Text("Name: ${state.userName}")
                Text("Email: ${state.email}")

                Button(onClick = { viewModel.onEditClicked() }) {
                    Text("Edit Profile")
                }
                Button(onClick = { viewModel.onBackClicked() }) {
                    Text("Back")
                }
            }
            is UserUiState.Error -> {
                Text("Error: ${state.message}")
                Button(onClick = { viewModel.onBackClicked() }) {
                    Text("Back")
                }
            }
        }
    }
}

@Composable
@NavigationDestination(EditUserScreen::class)
fun EditUserScreenDestination() {
    val navigation = navigationHandle<EditUserScreen>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Editing user: ${navigation.key.userId}")
        Button(onClick = { navigation.close() }) {
            Text("Save & Go Back")
        }
    }
}
