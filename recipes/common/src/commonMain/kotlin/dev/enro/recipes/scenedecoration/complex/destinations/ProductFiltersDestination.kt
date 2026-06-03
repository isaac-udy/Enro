package dev.enro.recipes.scenedecoration.complex.destinations

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.recipes.scenedecoration.complex.rightPane
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination
import kotlinx.serialization.Serializable

/**
 * Tagged `rightPane()`. When this is on top of the backstack the strategy
 * places it in the right slot on Wide, replaces the left pane with it on
 * Medium (right takes precedence), or shows it alone on Mobile.
 */
@Serializable
data object ProductFilters : NavigationKey

@OptIn(ExperimentalLayoutApi::class)
@NavigationDestination(ProductFilters::class)
val productFiltersDestination: NavigationDestinationProvider<ProductFilters> = navigationDestination(
    metadata = { rightPane() },
) {
    Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Filters", style = MaterialTheme.typography.titleLarge)
            Text(
                "Tagged rightPane(). On Wide windows this shows to the right of the " +
                    "product detail (with the list visible on the left). On Medium, this " +
                    "is shown to the right of the detail with the list hidden. On Mobile " +
                    "it fills the screen.",
                style = MaterialTheme.typography.bodyMedium,
            )
            var selected by remember { mutableStateOf(setOf<String>()) }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Price", "Brand", "Rating", "In stock", "On sale").forEach { filter ->
                    FilterChip(
                        selected = filter in selected,
                        onClick = {
                            selected = if (filter in selected) selected - filter else selected + filter
                        },
                        label = { Text(filter) },
                    )
                }
            }
            Button(onClick = { navigation.close() }) { Text("Close") }
        }
    }
}
