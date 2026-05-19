package dev.enro.recipes.scenedecoration.complex.destinations

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.navigationHandle
import dev.enro.open
import kotlinx.serialization.Serializable

/**
 * Untagged detail destination. When pushed on top of [ProductList], it
 * fills the main slot; on Wide / Medium the list shows alongside it in the
 * left pane. Pushing [ProductFilters] adds a right pane; pushing
 * [ImmersiveImage] takes over the whole content area.
 */
@Serializable
data class ProductDetail(val productId: String) : NavigationKey

@Composable
@NavigationDestination(ProductDetail::class)
fun ProductDetailDestination() {
    val navigation = navigationHandle<ProductDetail>()
    val product = shellProducts.firstOrNull { it.id == navigation.key.productId }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            product?.name ?: navigation.key.productId,
            style = MaterialTheme.typography.headlineMedium,
        )
        product?.price?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
        Text(
            "An untagged destination — fills the main slot. On Wide / Medium windows the " +
                "Shop list is visible alongside this as the left pane. Push the filters " +
                "panel for a right pane, or the immersive image to claim the entire " +
                "content area.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { navigation.open(ProductFilters) }) { Text("Open filters") }
            Button(onClick = { navigation.open(ImmersiveImage(navigation.key.productId)) }) {
                Text("View image")
            }
        }
    }
}
