// Snippets for docs/ghpages/docs/advanced/results/managed-result-flows.md
package dev.enro.docs.advanced.results.managed

import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.destinations.managedFlowDestination
import kotlinx.serialization.Serializable

@Serializable
data class BookingDetails(
    val destination: String,
    val date: String,
    val passengers: Int,
    val seat: String,
)

@Serializable
data object BookingFlow : NavigationKey.WithResult<BookingDetails>

@Serializable
data object SelectFlightDestination : NavigationKey.WithResult<String>

@Serializable
data object SelectDate : NavigationKey.WithResult<String>

@Serializable
data class SelectPassengers(val maxPassengers: Int) : NavigationKey.WithResult<Int>

@Serializable
data class SelectSeat(val availableSeats: List<String>) : NavigationKey.WithResult<String>

@Serializable
sealed interface Account

@Serializable
data class AdminAccount(val name: String, val token: String) : Account

@Serializable
data class StandardAccount(val name: String) : Account

@Serializable
data object CreateAccount : NavigationKey.WithResult<Account>

@Serializable
data object EnterName : NavigationKey.WithResult<String>

@Serializable
data object RequestAdminToken : NavigationKey.WithResult<String>

// region docs:managed-result-flows/flow
@NavigationDestination(BookingFlow::class)
val bookingFlowDestination: NavigationDestinationProvider<BookingFlow> =
    managedFlowDestination<BookingFlow, BookingDetails>(
        flow = {
            val destination = open(SelectFlightDestination)
            val date        = open(SelectDate)
            val passengers  = open(SelectPassengers(maxPassengers = 5))

            val seats = when {
                passengers > 3 -> listOf("Group Seating")
                else           -> listOf("12A", "12B", "14C")
            }
            val seat = open(SelectSeat(availableSeats = seats))

            BookingDetails(destination, date, passengers, seat)
        },
    )
// endregion

@NavigationDestination(CreateAccount::class)
val createAccountDestination = managedFlowDestination<CreateAccount, Account>(
    // region docs:managed-result-flows/branching
    flow = {
        val name = open(EnterName)
        if (name == "admin") {
            val token = open(RequestAdminToken)
            AdminAccount(name, token)
        } else {
            StandardAccount(name)
        }
    }
    // endregion
)
