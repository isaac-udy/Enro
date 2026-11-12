// Snippets for docs/ghpages/docs/advanced/view-models.md
package dev.enro.docs.advanced.viewmodels.dependencies

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.viewmodel.createEnroViewModel

class UserRepository

class Analytics

object MyContainer {
    val userRepository = UserRepository()
    val analytics = Analytics()
}

class ProfileViewModel(
    val repository: UserRepository,
    val analytics: Analytics,
) : ViewModel()

@Composable
fun ProfileScreen() {
    // region docs:view-models/create-with-dependencies
    val viewModel = viewModel<ProfileViewModel> {
        createEnroViewModel {
            ProfileViewModel(
                repository = MyContainer.userRepository,
                analytics  = MyContainer.analytics,
            )
        }
    }
    // endregion
}
