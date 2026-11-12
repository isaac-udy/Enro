/**
 * Enro Recipe: Flow in a ViewModel
 *
 * Shows a multi-step flow declared in a ViewModel with `registerForFlowResult`.
 *
 * The `flow` block declares the steps as sequential code. Each `open(key)`
 * opens a `NavigationKey.WithResult` screen and returns the result of the
 * screen. The flow runs the `flow` block again after each result, so the
 * block can branch on earlier results. When the `flow` block returns,
 * `onCompleted` receives the returned value.
 *
 * The host screen displays the steps in a container created with
 * `rememberNavigationContainerForFlow(viewModel.flow)`. Closing a step
 * returns to the previous step, where the user can choose a different result.
 *
 * `managedFlowDestination` (see the Managed Flow recipe) declares a flow
 * without a host screen. Use `registerForFlowResult` when the host screen
 * shows UI around the steps, such as a progress indicator, or has other
 * ViewModel state.
 */
package dev.enro.recipes.flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.recipes.RecipeScaffold
import dev.enro.result.flow.registerForFlowResult
import dev.enro.result.flow.rememberNavigationContainerForFlow
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import dev.enro.viewmodel.createEnroViewModel
import kotlinx.serialization.Serializable

@Serializable
object FlowInViewModelRecipe : NavigationKey

@Serializable
data object FlowStart : NavigationKey

@Serializable
data class Booking(
    val destination: String,
    val nights: Int,
    val breakfast: Boolean,
)

/** The key of the screen that hosts the flow. The screen completes with the [Booking]. */
@Serializable
data object CreateBooking : NavigationKey.WithResult<Booking>

@Serializable
data object ChooseDestination : NavigationKey.WithResult<String>

@Serializable
data class ChooseNights(val destination: String) : NavigationKey.WithResult<Int>

@Serializable
data object ChooseBreakfast : NavigationKey.WithResult<Boolean>

// region docs:flows/view-model
class CreateBookingViewModel : ViewModel() {
    private val navigation by navigationHandle<CreateBooking>()

    val flow by registerForFlowResult(
        flow = {
            val destination = open(ChooseDestination)
            val nights = open(ChooseNights(destination))
            // A branch: the flow opens ChooseBreakfast only for two nights or more.
            val breakfast = if (nights >= 2) open(ChooseBreakfast) else false
            Booking(destination, nights, breakfast)
        },
        onCompleted = { booking ->
            navigation.complete(booking)
        },
    )
}
// endregion

@Composable
@NavigationDestination(FlowInViewModelRecipe::class)
fun FlowInViewModelRecipeScreen() {
    val navigation = navigationHandle<FlowInViewModelRecipe>()
    RecipeScaffold(
        title = "Flow in a ViewModel",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(FlowStart.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(FlowStart::class)
fun FlowStartDestination() {
    var lastBooking by rememberSaveable { mutableStateOf("No booking yet") }
    val createBooking = registerForNavigationResult<Booking>(
        onClosed = { lastBooking = "Booking cancelled" },
        onCompleted = { booking ->
            val breakfast = if (booking.breakfast) "with breakfast" else "no breakfast"
            lastBooking = "${booking.destination}, ${booking.nights} night(s), $breakfast"
        },
    )
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Bookings", style = MaterialTheme.typography.titleLarge)
        Text(lastBooking)
        Button(onClick = { createBooking.open(CreateBooking) }) {
            Text("Create a booking")
        }
    }
}

// region docs:flows/host
@Composable
@NavigationDestination(CreateBooking::class)
fun CreateBookingDestination() {
    val navigation = navigationHandle<CreateBooking>()
    val viewModel = viewModel<CreateBookingViewModel> {
        createEnroViewModel { CreateBookingViewModel() }
    }
    // flowContainer displays the steps of the flow. The backstack of flowContainer
    // contains one entry for each step that has a result, plus the current step.
    val flowContainer = rememberNavigationContainerForFlow(viewModel.flow)

    Column(modifier = Modifier.fillMaxSize()) {
        LinearProgressIndicator(
            progress = { flowContainer.backstack.size / 3f },
            modifier = Modifier.fillMaxWidth(),
        )
        NavigationDisplay(
            state = flowContainer,
            modifier = Modifier.weight(1f),
        )
        Button(
            onClick = { navigation.close() },
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Cancel booking")
        }
    }
}
// endregion

@Composable
@NavigationDestination(ChooseDestination::class)
fun ChooseDestinationDestination() {
    val navigation = navigationHandle<ChooseDestination>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Where to?", style = MaterialTheme.typography.titleLarge)
        listOf("Lisbon", "Kyoto", "Wellington").forEach { place ->
            Button(onClick = { navigation.complete(place) }) { Text(place) }
        }
    }
}

@Composable
@NavigationDestination(ChooseNights::class)
fun ChooseNightsDestination() {
    val navigation = navigationHandle<ChooseNights>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("How many nights in ${navigation.key.destination}?", style = MaterialTheme.typography.titleLarge)
        listOf(1, 2, 5).forEach { nights ->
            Button(onClick = { navigation.complete(nights) }) { Text("$nights") }
        }
        Button(onClick = { navigation.close() }) { Text("Back") }
    }
}

@Composable
@NavigationDestination(ChooseBreakfast::class)
fun ChooseBreakfastDestination() {
    val navigation = navigationHandle<ChooseBreakfast>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Add breakfast?", style = MaterialTheme.typography.titleLarge)
        Button(onClick = { navigation.complete(true) }) { Text("Yes") }
        Button(onClick = { navigation.complete(false) }) { Text("No") }
        Button(onClick = { navigation.close() }) { Text("Back") }
    }
}
