/**
 * Enro Recipe: Basic ViewModel
 *
 * Nav3 equivalent: "Basic ViewModel" recipe
 * https://nicbell.github.io/nav3/recipes/basic-viewmodel
 *
 * Demonstrates how to use NavigationHandle in a ViewModel in Enro,
 * compared to Nav3's approach of accessing the key via SavedStateHandle or ViewModel parameters.
 *
 * Key differences from Nav3:
 * - Nav3's ViewModel receives the key through standard ViewModel mechanisms (SavedStateHandle, constructor).
 *   Navigation is performed by mutating the backstack list from the composable layer.
 * - Enro provides a `navigationHandle()` delegate for ViewModels that gives direct access to
 *   the NavigationKey AND the ability to perform navigation operations from the ViewModel.
 * - This means ViewModels can call `navigation.open(...)` and `navigation.close()` directly,
 *   without needing to expose navigation events as state for the UI to consume.
 * - The ViewModel is automatically scoped to its NavigationKey.Instance and survives config changes.
 */
package dev.enro.recipes.viewmodel

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.viewmodel.createEnroViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// -- Navigation Keys --

@Serializable
data class UserScreen(val userId: String) : NavigationKey

@Serializable
data class EditUserScreen(val userId: String) : NavigationKey

// -- ViewModel --
// Nav3 equivalent: A standard ViewModel that receives the key via constructor/SavedStateHandle.
// In Nav3, the ViewModel cannot navigate -- it exposes events that the UI layer handles.
//
// Enro: The ViewModel gets a typed NavigationHandle via the `by navigationHandle()` delegate.
// It can read the key's properties AND perform navigation operations.

class UserViewModel : ViewModel() {
    // The navigationHandle delegate provides:
    // 1. Access to the NavigationKey (navigation.key) with full type safety
    // 2. Ability to navigate (navigation.open(), navigation.close())
    // 3. Lifecycle awareness (tied to the ViewModel's scope)
    private val navigation: NavigationHandle<UserScreen> by navigationHandle()

    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            // Simulate network call
            delay(1000)
            _uiState.value = UserUiState.Success(
                userName = "User ${navigation.key.userId}",
                email = "${navigation.key.userId}@example.com",
            )
        }
    }

    fun onEditClicked() {
        // Nav3: You'd emit a navigation event that the UI observes and acts on.
        // Enro: Navigate directly from the ViewModel.
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

// -- Destination --
// The ViewModel is created with createEnroViewModel which provides the navigation handle.

@Composable
@NavigationDestination(UserScreen::class)
fun UserScreenDestination() {
    // createEnroViewModel ensures the NavigationHandle is available to the ViewModel
    // before any of its properties (like `by navigationHandle()`) are accessed.
    val viewModel = viewModel<UserViewModel> { createEnroViewModel { UserViewModel() } }
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
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

// -- Edit Screen (simple, no ViewModel) --

@Composable
@NavigationDestination(EditUserScreen::class)
fun EditUserScreenDestination() {
    val navigation = navigationHandle<EditUserScreen>()
    Column {
        Text("Editing user: ${navigation.key.userId}")
        Button(onClick = { navigation.close() }) {
            Text("Save & Go Back")
        }
    }
}
