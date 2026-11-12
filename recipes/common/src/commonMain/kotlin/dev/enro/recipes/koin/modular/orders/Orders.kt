// The orders feature of the Koin Modular recipe. See KoinModularApp.kt.
package dev.enro.recipes.koin.modular.orders

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
import dev.enro.recipes.koin.koinEnroViewModel
import dev.enro.recipes.koin.modular.KoinAccount
import dev.enro.recipes.koin.modular.KoinOrders
import dev.enro.recipes.koin.modular.KoinSession
import org.koin.dsl.module

class OrderRepository {
    fun orders(): List<String> = listOf("Order 1001", "Order 1002")
}

// region docs:di/koin-modular-feature
class OrdersViewModel(
    private val session: KoinSession,
    repository: OrderRepository,
) : ViewModel() {
    private val navigation by navigationHandle<KoinOrders>()

    val orders: List<String> = repository.orders()
    val userName: String get() = session.userName

    // The account feature declares the destination for KoinAccount. The
    // orders feature imports the key only.
    fun onAccountClicked() = navigation.open(KoinAccount)
}

// The Koin module of the orders feature. The app's module provides KoinSession.
val ordersModule = module {
    single { OrderRepository() }
    factory { OrdersViewModel(session = get(), repository = get()) }
}

@Composable
@NavigationDestination(KoinOrders::class)
fun KoinOrdersDestination() {
    val viewModel = koinEnroViewModel<OrdersViewModel>()
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
