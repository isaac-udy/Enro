// Snippets for docs/ghpages/docs/advanced/results.md
package dev.enro.docs.advanced.results

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.ui.destinations.completeFrom
import dev.enro.ui.destinations.syntheticDestination
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

private val today = LocalDate(2026, 1, 1)

@Serializable
data class SelectDate(
    val minDate: LocalDate? = null,
    val maxDate: LocalDate? = null,
) : NavigationKey.WithResult<LocalDate>

@Serializable
data object Home : NavigationKey

@Serializable
data class ProfileUpdate(val name: String)

private object featureFlags {
    val useNewProfileUi = true
}

// region docs:results/complete
@Composable
@NavigationDestination(SelectDate::class)
fun SelectDateScreen() {
    val navigation = navigationHandle<SelectDate>()
    // ...
    Button(onClick = { navigation.complete(today) }) { Text("Use today") }
}
// endregion

@Composable
fun Caller() {
    // region docs:results/channel
    val getDate = registerForNavigationResult<LocalDate>(
        onCompleted = { date -> /* use the date */ },
    )

    Button(onClick = { getDate.open(SelectDate()) }) { Text("Pick a date") }
    // endregion
}

// region docs:results/composable
@Composable
@NavigationDestination(Home::class)
fun HomeScreen() {
    val navigation = navigationHandle<Home>()

    val pickDate = registerForNavigationResult<LocalDate>(
        onClosed = { /* SelectDate closed without a result */ },
        onCompleted = { date ->
            // onCompleted is a callback, not a suspending function.
            // Compose state can be updated here.
        },
    )

    Button(onClick = { pickDate.open(SelectDate(maxDate = today)) }) {
        Text("Pick a date")
    }
}
// endregion

// region docs:results/no-result-key
@Serializable
data class ConfirmDelete(val itemName: String) : NavigationKey
// endregion

@Composable
fun ConfirmDeleteCaller() {
    // region docs:results/no-result-channel
    val confirmDelete = registerForNavigationResult(
        onCompleted = { /* user confirmed */ },
        onClosed    = { /* user cancelled */ },
    )

    Button(onClick = { confirmDelete.open(ConfirmDelete("Tax return.pdf")) }) {
        Text("Delete")
    }
    // endregion
}

// region docs:results/view-model
class HomeViewModel : ViewModel() {
    val navigation by navigationHandle<Home>()

    val pickDate by registerForNavigationResult<LocalDate>(
        onCompleted = { date ->
            // Update a StateFlow or call a use case here.
        },
    )

    fun onPickDateClicked() {
        pickDate.open(SelectDate(maxDate = today))
    }
}
// endregion

// region docs:results/decider
@Serializable object EditProfile : NavigationKey.WithResult<ProfileUpdate>
@Serializable object EditProfileLegacy : NavigationKey.WithResult<ProfileUpdate>
@Serializable object EditProfileV2 : NavigationKey.WithResult<ProfileUpdate>

@NavigationDestination(EditProfile::class)
val editProfile = syntheticDestination<EditProfile> {
    if (featureFlags.useNewProfileUi) completeFrom(EditProfileV2)
    else completeFrom(EditProfileLegacy)
}
// endregion
