/**
 * Enro Recipe: Managed Flow
 *
 * NO Nav3 equivalent -- this is a unique Enro feature.
 *
 * Demonstrates Enro's managedFlowDestination, which allows you to define multi-step
 * navigation flows as sequential, imperative code. Each step opens a destination,
 * suspends until a result is returned, and continues with the next step.
 *
 * This is one of Enro's most distinctive features. Nav3 has no equivalent because it
 * requires you to manage flow state manually (tracking which step you're on, collecting
 * results from each step, and deciding what to do next).
 *
 * Key advantages of managed flows:
 * - Multi-step flows are defined as simple, readable sequential code
 * - Each step's result is type-safe and available as a local variable
 * - The flow automatically handles back navigation (going back reverts to the previous step)
 * - Flow state survives configuration changes and process death
 * - The flow can include conditional logic, loops, and async operations
 * - The flow's steps are rendered in their own NavigationContainer
 */
package dev.enro.recipes.managedflow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.enro.NavigationKey
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.destinations.managedFlowDestination
import kotlinx.serialization.Serializable

// -- Result type --
@Serializable
data class BookingDetails(
    val destination: String,
    val date: String,
    val passengers: Int,
    val seatPreference: String,
)

// -- Flow key --
// The flow itself is a NavigationKey.WithResult that returns the final result.
@Serializable
data object BookingFlow : NavigationKey.WithResult<BookingDetails>

// -- Step keys --
// Each step in the flow is its own NavigationKey.WithResult.
// They are independent destinations that can also be used outside the flow.

@Serializable
data object SelectDestination : NavigationKey.WithResult<String>

@Serializable
data object SelectDate : NavigationKey.WithResult<String>

@Serializable
data class SelectPassengers(val maxPassengers: Int = 10) : NavigationKey.WithResult<Int>

@Serializable
data class SelectSeat(val availableSeats: List<String>) : NavigationKey.WithResult<String>

// -- Managed Flow Definition --
// This is where the magic happens. The flow is defined as sequential code.
// Each `open()` call opens a destination, suspends until the user provides a result,
// and then continues with the next line.

@OptIn(ExperimentalEnroApi::class)
@NavigationDestination(BookingFlow::class)
val bookingFlowDestination: NavigationDestinationProvider<BookingFlow> =
    managedFlowDestination<BookingFlow, BookingDetails>(
        flow = {
            // Step 1: Select destination
            val destination = open(SelectDestination)

            // Step 2: Select date
            val date = open(SelectDate)

            // Step 3: Select number of passengers
            val passengers = open(SelectPassengers(maxPassengers = 5))

            // Step 4: Select seat preference
            // Notice how we can use results from previous steps to configure later steps.
            val availableSeats = when {
                passengers > 3 -> listOf("Group Seating")
                else -> listOf("Window", "Aisle", "Middle")
            }
            val seat = open(SelectSeat(availableSeats))

            // The flow returns the composed result.
            // This result is delivered to whoever opened the BookingFlow key.
            BookingDetails(
                destination = destination,
                date = date,
                passengers = passengers,
                seatPreference = seat,
            )
        },
    )

// -- Managed Flow with async steps --
// Flows can also include async operations using the `async` function.

@Serializable
data object OnboardingFlow : NavigationKey

@Serializable
data object WelcomeScreen : NavigationKey.WithResult<Unit>

@Serializable
data class EnterName(val placeholder: String = "") : NavigationKey.WithResult<String>

@Serializable
data object AcceptTerms : NavigationKey.WithResult<Boolean>

@OptIn(ExperimentalEnroApi::class)
@NavigationDestination(OnboardingFlow::class)
val onboardingFlowDestination: NavigationDestinationProvider<OnboardingFlow> =
    managedFlowDestination<OnboardingFlow, Unit>(
        flow = {
            // Step 1: Welcome screen (just acknowledgment, no data)
            open(WelcomeScreen)

            // Step 2: Enter name
            val name = open(EnterName(placeholder = "Your name"))

            // Step 3: Accept terms
            val accepted = open(AcceptTerms)

            if (!accepted) {
                // If the user declines, the flow can handle it.
                // In this case we just complete with Unit.
            }

            // The flow result (Unit in this case) completes the onboarding.
        },
        onCompleted = {
            // After the flow completes, close the flow destination.
            navigation.close()
        }
    )

// -- Step Destinations --
// These are normal destinations. They don't know they're part of a flow.
// They can be reused in other contexts.

@Composable
@NavigationDestination(SelectDestination::class)
fun SelectDestinationScreen() {
    val navigation = navigationHandle<SelectDestination>()

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Where do you want to go?")

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

    Column(modifier = Modifier.fillMaxSize()) {
        Text("When do you want to travel?")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@NavigationDestination(SelectPassengers::class)
fun SelectPassengersScreen() {
    val navigation = navigationHandle<SelectPassengers>()
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableStateOf(1) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("How many passengers?")

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            TextField(
                value = selected.toString(),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                (1..navigation.key.maxPassengers).forEach { count ->
                    DropdownMenuItem(
                        text = { Text("$count") },
                        onClick = {
                            selected = count
                            expanded = false
                        },
                    )
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

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Select your seat preference:")

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

@Composable
@NavigationDestination(WelcomeScreen::class)
fun WelcomeScreenDestination() {
    val navigation = navigationHandle<WelcomeScreen>()
    Column {
        Text("Welcome to the app!")
        Button(onClick = { navigation.complete(Unit) }) {
            Text("Get Started")
        }
    }
}

@Composable
@NavigationDestination(EnterName::class)
fun EnterNameDestination() {
    val navigation = navigationHandle<EnterName>()
    var name by rememberSaveable { mutableStateOf(navigation.key.placeholder) }

    Column {
        Text("What's your name?")
        TextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { navigation.complete(name) },
            enabled = name.isNotBlank(),
        ) {
            Text("Continue")
        }
    }
}

@Composable
@NavigationDestination(AcceptTerms::class)
fun AcceptTermsDestination() {
    val navigation = navigationHandle<AcceptTerms>()
    Column {
        Text("Do you accept the terms and conditions?")
        Button(onClick = { navigation.complete(true) }) {
            Text("Accept")
        }
        Button(onClick = { navigation.complete(false) }) {
            Text("Decline")
        }
    }
}

// -- Usage --
// To start the booking flow from any screen:
//
// val bookingResult = registerForNavigationResult<BookingDetails> { details ->
//     // Handle the completed booking
//     println("Booked: ${details.destination} on ${details.date}")
// }
// Button(onClick = { bookingResult.open(BookingFlow) }) { Text("Book a Trip") }
