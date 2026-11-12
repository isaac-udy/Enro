package dev.enro.recipes.scenedecoration.complex.destinations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination
import dev.enro.ui.scenes.directOverlay
import kotlinx.serialization.Serializable

/**
 * The destination sets the `directOverlay()` metadata from
 * `dev.enro.ui.scenes`. The recipe registers `ShellOverlaySceneStrategy` in
 * place of `DirectOverlaySceneStrategy`. `ShellOverlaySceneStrategy` renders
 * the destination as a right-side drawer with a scrim on the Wide and Medium
 * breakpoints, and as a bottom sheet on Mobile.
 */
@Serializable
data object CartOverlay : NavigationKey

@NavigationDestination(CartOverlay::class)
val cartOverlayDestination: NavigationDestinationProvider<CartOverlay> = navigationDestination(
    metadata = { directOverlay() },
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Your Cart", style = MaterialTheme.typography.titleLarge)
        Text(
            "Rendered via ShellOverlaySceneStrategy — drawer on Wide / Medium, bottom " +
                "sheet on Mobile. Tap the scrim to dismiss.",
            style = MaterialTheme.typography.bodyMedium,
        )
        HorizontalDivider()
        listOf(
            "Espresso Machine — \$299",
            "Coffee Beans — \$24",
        ).forEach { line ->
            Text(line, style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { navigation.close() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Close")
        }
    }
}
