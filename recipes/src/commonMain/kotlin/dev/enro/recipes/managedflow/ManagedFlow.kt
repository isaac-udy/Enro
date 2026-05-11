/**
 * Enro Recipe: Managed Flow
 *
 * Demonstrates Enro's managedFlowDestination, which allows multi-step navigation
 * flows to be defined as sequential, imperative code.
 */
package dev.enro.recipes.managedflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.recipes.RecipeScaffold
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.destinations.managedFlowDestination
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object ManagedFlowRecipe : NavigationKey

@Serializable
data class BookingDetails(
    val destination: String,
    val date: String,
    val passengers: Int,
    val seatPreference: String,
)

@Serializable
data object BookingFlow : NavigationKey.WithResult<BookingDetails>

@Serializable
data object SelectFlightDestination : NavigationKey.WithResult<String>

@Serializable
data object SelectDate : NavigationKey.WithResult<String>

@Serializable
data class SelectPassengers(val maxPassengers: Int = 10) : NavigationKey.WithResult<Int>

@Serializable
data class SelectSeat(val availableSeats: List<String>) : NavigationKey.WithResult<String>

@Serializable
data object ManagedFlowHome : NavigationKey

@Composable
@NavigationDestination(ManagedFlowRecipe::class)
fun ManagedFlowRecipeScreen() {
    val navigation = navigationHandle<ManagedFlowRecipe>()
    RecipeScaffold(
        title = "Managed Flow",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(ManagedFlowHome.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(ManagedFlowHome::class)
fun ManagedFlowHomeDestination() {
    var lastBooking by rememberSaveable { mutableStateOf<String?>(null) }
    val bookingResult = registerForNavigationResult<BookingDetails> { details ->
        lastBooking = "${details.destination} on ${details.date} for ${details.passengers} (${details.seatPreference})"
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Managed Flow Recipe")
        Text("Last booking: ${lastBooking ?: "none"}")
        Button(onClick = { bookingResult.open(BookingFlow) }) {
            Text("Start Booking Flow")
        }
    }
}

@OptIn(ExperimentalEnroApi::class)
@NavigationDestination(BookingFlow::class)
val bookingFlowDestination: NavigationDestinationProvider<BookingFlow> =
    managedFlowDestination<BookingFlow, BookingDetails>(
        flow = {
            val destination = open(SelectFlightDestination)
            val date = open(SelectDate)
            val passengers = open(SelectPassengers(maxPassengers = 5))

            val availableSeats = when {
                passengers > 3 -> listOf("Group Seating")
                else -> listOf("Window", "Aisle", "Middle")
            }
            val seat = open(SelectSeat(availableSeats))

            BookingDetails(
                destination = destination,
                date = date,
                passengers = passengers,
                seatPreference = seat,
            )
        },
    )

@Composable
@NavigationDestination(SelectFlightDestination::class)
fun SelectFlightDestinationScreen() {
    val navigation = navigationHandle<SelectFlightDestination>()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Where do you want to go?", style = MaterialTheme.typography.titleMedium)

        listOf("New York", "London", "Tokyo", "Sydney").forEach { city ->
            Button(
                onClick = { navigation.complete(city) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(city)
            }
        }
    }
}

@Composable
@NavigationDestination(SelectDate::class)
fun SelectDateScreen() {
    val navigation = navigationHandle<SelectDate>()
    var date by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("When do you want to travel?", style = MaterialTheme.typography.titleMedium)
        TextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { navigation.complete(date) },
            enabled = date.isNotBlank(),
        ) {
            Text("Continue")
        }
    }
}

@Composable
@NavigationDestination(SelectPassengers::class)
fun SelectPassengersScreen() {
    val navigation = navigationHandle<SelectPassengers>()
    var selected by rememberSaveable { mutableStateOf(1) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("How many passengers?", style = MaterialTheme.typography.titleMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Selected: $selected", style = MaterialTheme.typography.titleSmall)
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    (1..navigation.key.maxPassengers).forEach { count ->
                        Button(onClick = { selected = count }) {
                            Text("$count")
                        }
                    }
                }
            }
        }

        Button(onClick = { navigation.complete(selected) }) {
            Text("Continue")
        }
    }
}

@Composable
@NavigationDestination(SelectSeat::class)
fun SelectSeatScreen() {
    val navigation = navigationHandle<SelectSeat>()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Select your seat preference:", style = MaterialTheme.typography.titleMedium)

        navigation.key.availableSeats.forEach { seat ->
            Button(
                onClick = { navigation.complete(seat) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(seat)
            }
        }
    }
}
