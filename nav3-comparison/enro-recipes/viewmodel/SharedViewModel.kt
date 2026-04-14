/**
 * Enro Recipe: Shared ViewModel
 *
 * Nav3 equivalent: "Shared ViewModel" recipe
 * https://nicbell.github.io/nav3/recipes/shared-viewmodel
 *
 * Demonstrates how to share state between parent and child destinations in Enro.
 *
 * Key differences from Nav3:
 * - Nav3 suggests sharing a ViewModel across entries by scoping it to a common owner
 *   (e.g., the Activity or a parent NavDisplay).
 * - Enro takes a different approach: instead of sharing ViewModels, it encourages
 *   using NavigationKey properties to pass data forward and NavigationResults to pass
 *   data back. This keeps destinations decoupled.
 * - For truly shared state, Enro destinations can access a parent's ViewModel via
 *   standard Compose/Android scoping (e.g., LocalViewModelStoreOwner or by passing
 *   a shared state holder through CompositionLocal).
 * - Enro's managed flows provide another pattern: the flow orchestrates multiple
 *   destinations and collects their results, acting as the "shared" coordinator.
 *
 * This recipe shows multiple approaches to sharing state.
 */
package dev.enro.recipes.viewmodel

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.result.registerForNavigationResult
import dev.enro.viewmodel.createEnroViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

// ============================================================
// Approach 1: Pass data via NavigationKey + Results (Recommended)
// ============================================================
// This is the most "Enro-native" approach. Data flows forward via key properties
// and backward via NavigationResults.

@Serializable
data object OrderFlow : NavigationKey

@Serializable
data class SelectProduct(val category: String) : NavigationKey.WithResult<String>

@Serializable
data class ConfirmOrder(val productName: String) : NavigationKey.WithResult<Boolean>

@Composable
@NavigationDestination(OrderFlow::class)
fun OrderFlowDestination() {
    val navigation = navigationHandle<OrderFlow>()
    var selectedProduct by rememberSaveable { mutableStateOf<String?>(null) }
    var orderConfirmed by rememberSaveable { mutableStateOf(false) }

    // Register for results from child screens.
    // This is how data flows BACK from children without shared ViewModels.
    val selectProduct = registerForNavigationResult<String> { productName ->
        selectedProduct = productName
    }

    val confirmOrder = registerForNavigationResult<Boolean> { confirmed ->
        orderConfirmed = confirmed
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Order Flow")
        Text("Selected: ${selectedProduct ?: "None"}")
        Text("Confirmed: $orderConfirmed")

        Button(onClick = { selectProduct.open(SelectProduct("electronics")) }) {
            Text("Select Product")
        }

        if (selectedProduct != null) {
            Button(onClick = { confirmOrder.open(ConfirmOrder(selectedProduct!!)) }) {
                Text("Confirm Order")
            }
        }
    }
}

@Composable
@NavigationDestination(SelectProduct::class)
fun SelectProductDestination() {
    val navigation = navigationHandle<SelectProduct>()
    Column {
        Text("Select a product from: ${navigation.key.category}")
        Button(onClick = { navigation.complete("Laptop") }) {
            Text("Laptop")
        }
        Button(onClick = { navigation.complete("Phone") }) {
            Text("Phone")
        }
    }
}

@Composable
@NavigationDestination(ConfirmOrder::class)
fun ConfirmOrderDestination() {
    val navigation = navigationHandle<ConfirmOrder>()
    Column {
        Text("Confirm order for: ${navigation.key.productName}?")
        Button(onClick = { navigation.complete(true) }) {
            Text("Confirm")
        }
        Button(onClick = { navigation.complete(false) }) {
            Text("Cancel")
        }
    }
}

// ============================================================
// Approach 2: CompositionLocal for truly shared state
// ============================================================
// When multiple destinations need live access to the same mutable state,
// you can use a CompositionLocal to provide a shared state holder.
// This is closer to Nav3's "shared ViewModel" pattern.

class SharedCartState {
    private val _items = MutableStateFlow<List<String>>(emptyList())
    val items: StateFlow<List<String>> = _items.asStateFlow()

    fun addItem(item: String) {
        _items.value = _items.value + item
    }

    fun removeItem(item: String) {
        _items.value = _items.value - item
    }

    fun clear() {
        _items.value = emptyList()
    }
}

val LocalCartState = compositionLocalOf<SharedCartState> {
    error("No SharedCartState provided")
}

// In the host, provide the shared state:
// CompositionLocalProvider(LocalCartState provides sharedCartState) {
//     NavigationDisplay(state = container)
// }
//
// In any destination:
// val cart = LocalCartState.current
// cart.addItem("New Item")
// val items by cart.items.collectAsState()

// ============================================================
// Approach 3: Parent ViewModel access via createEnroViewModel
// ============================================================
// A parent destination's ViewModel can be accessed by child destinations
// by using the standard Compose ViewModel scoping mechanisms.

class ParentViewModel : ViewModel() {
    private val navigation by navigationHandle<ParentScreen>()

    private val _sharedData = MutableStateFlow("Initial shared data")
    val sharedData: StateFlow<String> = _sharedData.asStateFlow()

    fun updateData(newData: String) {
        _sharedData.value = newData
    }

    fun openChild() {
        // Pass current state forward via the key.
        navigation.open(ChildScreen(currentData = _sharedData.value))
    }
}

@Serializable
data object ParentScreen : NavigationKey

@Serializable
data class ChildScreen(val currentData: String) : NavigationKey.WithResult<String>

@Composable
@NavigationDestination(ParentScreen::class)
fun ParentScreenDestination() {
    val viewModel = viewModel<ParentViewModel> { createEnroViewModel { ParentViewModel() } }
    val data by viewModel.sharedData.collectAsState()

    // When the child returns a result, update the shared state.
    val openChild = registerForNavigationResult<String> { updatedData ->
        viewModel.updateData(updatedData)
    }

    Column {
        Text("Parent - Shared data: $data")
        Button(onClick = { openChild.open(ChildScreen(data)) }) {
            Text("Open Child")
        }
    }
}

@Composable
@NavigationDestination(ChildScreen::class)
fun ChildScreenDestination() {
    val navigation = navigationHandle<ChildScreen>()
    var editedData by rememberSaveable { mutableStateOf(navigation.key.currentData) }

    Column {
        Text("Child - Editing shared data")
        TextField(
            value = editedData,
            onValueChange = { editedData = it },
        )
        Button(onClick = { navigation.complete(editedData) }) {
            Text("Save & Return")
        }
        Button(onClick = { navigation.close() }) {
            Text("Cancel")
        }
    }
}
