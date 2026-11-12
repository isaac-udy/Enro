// Snippets for docs/ghpages/docs/getting-started/basic-concepts.md
package dev.enro.docs.gettingstarted.basicconcepts

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationComponent
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.complete
import dev.enro.controller.NavigationComponentConfiguration
import dev.enro.controller.createNavigationModule
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

private val today = LocalDate(2026, 1, 1)

@Serializable
data object Home : NavigationKey

@Composable
@NavigationDestination(Home::class)
fun HomeScreen() {
    // region docs:basic-concepts/results
    val getDate = registerForNavigationResult<LocalDate>(
        onCompleted = { date -> /* use date */ },
    )

    Button(onClick = { getDate.open(SelectDate(maxDate = today)) }) {
        Text("Pick a date")
    }
    // endregion
}

// region docs:basic-concepts/keys
@Serializable
data class ShowProfile(val userId: String) : NavigationKey

@Serializable
data class SelectDate(
    val minDate: LocalDate? = null,
    val maxDate: LocalDate? = null,
) : NavigationKey.WithResult<LocalDate>
// endregion

// region docs:basic-concepts/composable-destination
@Composable
@NavigationDestination(ShowProfile::class)
fun ProfileScreen() {
    val navigation = navigationHandle<ShowProfile>()
    Text("Profile for ${navigation.key.userId}")
}
// endregion

@Composable
@NavigationDestination(SelectDate::class)
fun SelectDateScreen() {
    // region docs:basic-concepts/handle
    val navigation = navigationHandle<SelectDate>()

    navigation.open(ShowProfile("user-123"))
    navigation.close()
    navigation.complete(today) // closes SelectDate and returns `today` to the caller
    // endregion
}

@Composable
fun RootContent() {
    // region docs:basic-concepts/container
    val container = rememberNavigationContainer(
        backstack = backstackOf(Home.asInstance()),
    )
    NavigationDisplay(state = container)
    // endregion
}

fun initialBackstack() {
    // region docs:basic-concepts/instance
    val initial = backstackOf(
        Home.asInstance(),
        ShowProfile("user-123").asInstance(),
    )
    // endregion
}

// region docs:basic-concepts/component
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule { /* optional config */ }
)
// endregion
