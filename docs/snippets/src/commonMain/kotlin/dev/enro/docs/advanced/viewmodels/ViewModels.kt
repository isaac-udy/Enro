// Snippets for docs/ghpages/docs/advanced/view-models.md
package dev.enro.docs.advanced.viewmodels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.requestClose
import dev.enro.viewmodel.createEnroViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

@Serializable
data class ShowProfile(val userId: String) : NavigationKey

@Serializable
data class EditProfile(val userId: String, val initial: String = "") : NavigationKey

// region docs:view-models/handle
class ProfileViewModel : ViewModel() {
    val navigation by navigationHandle<ShowProfile>()
    val uiState = MutableStateFlow("") // docs:hide

    fun onEditClicked() {
        navigation.open(EditProfile(navigation.key.userId))
    }

    fun onBackClicked() {
        navigation.requestClose()
    }
}
// endregion

// region docs:view-models/config
class EditProfileViewModel : ViewModel() {
    private val draft = MutableStateFlow("")

    val navigation by navigationHandle<EditProfile>(
        config = {
            onCloseRequested {
                if (draft.value == key.initial) close() else openConfirmationDialog()
            }
        },
    )

    private fun openConfirmationDialog() { /* ... */ }
}
// endregion

// region docs:view-models/create
@Composable
@NavigationDestination(ShowProfile::class)
fun ProfileScreen() {
    val viewModel = viewModel<ProfileViewModel> { createEnroViewModel { ProfileViewModel() } }
    val state by viewModel.uiState.collectAsState()
    // ...
}
// endregion
