package dev.enro.recipes.scenedecoration.complex.destinations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import kotlinx.serialization.Serializable

/**
 * Home section's root destination. Untagged — fills the main slot on every
 * breakpoint. Just an explainer page pointing users to the chrome's left
 * rail / bottom nav and the cart icon in the top bar.
 */
@Serializable
data object ShellHome : NavigationKey

@Composable
@NavigationDestination(ShellHome::class)
fun ShellHomeDestination() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Welcome to the Shell", style = MaterialTheme.typography.headlineMedium)
        Text(
            "This recipe demonstrates a metadata-driven multi-pane scene strategy. " +
                "Use the rail on the left (or the bottom nav on mobile) to switch " +
                "sections. Visit Shop to see the leftPane(), rightPane(), and " +
                "fullScreen() metadata tags in action, and tap the cart icon in the " +
                "top bar to open a directOverlay() destination.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            "Try resizing the window. Wide windows show all three pane slots, medium " +
                "shows main + one pane, mobile shows only the top destination.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
