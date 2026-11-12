// Snippets for docs/ghpages/docs/migrating-from-v2.md
package dev.enro.docs.migrating.results

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

private val today = LocalDate(2026, 1, 1)

@Serializable
data object Home : NavigationKey

@Serializable
data class SelectDate(val min: LocalDate? = null) : NavigationKey.WithResult<LocalDate>

// region docs:migrating/results
@Composable
@NavigationDestination(Home::class)
fun HomeScreen() {
    val getDate = registerForNavigationResult<LocalDate>(
        onCompleted = { date -> /* use the date */ },
        onClosed = { /* dismissed without a result */ },
    )

    Button(onClick = { getDate.open(SelectDate()) }) { Text("Pick a date") }
}

@Composable
@NavigationDestination(SelectDate::class)
fun SelectDateScreen() {
    val navigation = navigationHandle<SelectDate>()
    // ...
    Button(onClick = { navigation.complete(today) }) { Text("Use today") }
}
// endregion
