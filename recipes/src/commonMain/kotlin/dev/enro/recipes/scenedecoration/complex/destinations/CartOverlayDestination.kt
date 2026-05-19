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
 * Tagged with the built-in `directOverlay()` builder from
 * `dev.enro.ui.scenes`. The recipe's `ShellOverlaySceneStrategy` (registered
 * earlier in the chain than the default `DirectOverlaySceneStrategy`) wraps
 * the content as a right-side drawer on Wide / Medium breakpoints, or a
 * bottom sheet on Mobile, with a scrim that dismisses on tap.
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
