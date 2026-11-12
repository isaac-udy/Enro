// The orders feature of the Metro Modular recipe. See MetroModularApp.kt.
package dev.enro.recipes.metro.modular.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import dev.enro.annotations.NavigationDestination
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.metro.modular.MetroAccount
import dev.enro.recipes.metro.modular.MetroOrders
import dev.enro.recipes.metro.modular.MetroSession
import dev.enro.recipes.metro.modular.metroEnroViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject

@Inject
class OrderRepository {
    fun orders(): List<String> = listOf("Order 1001", "Order 1002")
}

// region docs:di/metro-modular-feature
@Inject
class OrdersViewModel(
    private val session: MetroSession,
    repository: OrderRepository,
) : ViewModel() {
    private val navigation by navigationHandle<MetroOrders>()

    val orders: List<String> = repository.orders()
    val userName: String get() = session.userName

    // The account screen is in another feature. It is opened by key.
    fun onAccountClicked() = navigation.open(MetroAccount)
}

// What this feature needs from the app's graph.
@ContributesTo(AppScope::class)
interface OrdersGraph {
    val ordersViewModel: OrdersViewModel
}

@Composable
@NavigationDestination(MetroOrders::class)
fun MetroOrdersDestination() {
    val viewModel = metroEnroViewModel<OrdersGraph, OrdersViewModel> { it.ordersViewModel }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Orders for ${viewModel.userName}", style = MaterialTheme.typography.titleLarge)
        viewModel.orders.forEach { Text(it) }
        Button(onClick = { viewModel.onAccountClicked() }) { Text("Account") }
    }
}
// endregion
