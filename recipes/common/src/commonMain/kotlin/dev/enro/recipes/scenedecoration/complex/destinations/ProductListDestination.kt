package dev.enro.recipes.scenedecoration.complex.destinations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.open
import dev.enro.recipes.scenedecoration.complex.leftPane
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination
import kotlinx.serialization.Serializable

/**
 * Shop section's root destination. Tagged `leftPane()` — when a detail
 * destination is pushed on top, this list collapses into the left companion
 * slot on Wide / Medium windows. On Mobile, the top destination fills the
 * content area and this list is only visible after a pop.
 */
@Serializable
data object ProductList : NavigationKey

internal data class ShellProduct(val id: String, val name: String, val price: String)

internal val shellProducts: List<ShellProduct> = listOf(
    ShellProduct("p1", "Espresso Machine", "$299"),
    ShellProduct("p2", "Coffee Beans (1kg)", "$24"),
    ShellProduct("p3", "Tea Set", "$45"),
    ShellProduct("p4", "Milk Frother", "$65"),
    ShellProduct("p5", "Pour-Over Kettle", "$89"),
)

@NavigationDestination(ProductList::class)
val productListDestination: NavigationDestinationProvider<ProductList> = navigationDestination(
    metadata = { leftPane() },
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Shop", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Tap a product to push it on top. On Wide / Medium windows the " +
                        "list collapses into the left pane and the detail fills the main slot.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            HorizontalDivider()
        }
        items(shellProducts, key = { it.id }) { product ->
            ListItem(
                headlineContent = { Text(product.name) },
                supportingContent = { Text(product.price) },
                modifier = Modifier.clickable {
                    navigation.open(ProductDetail(product.id))
                },
            )
            HorizontalDivider()
        }
    }
}
