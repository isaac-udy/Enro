package dev.enro.recipes.scenedecoration.complex.destinations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.recipes.scenedecoration.complex.fullScreen
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination
import kotlinx.serialization.Serializable

/**
 * Tagged `fullScreen()`. Short-circuits slot resolution to render only the
 * top destination, and on Mobile suppresses the bottom chrome (search + nav
 * row) for an immersive experience. The chrome's top bar still renders.
 */
@Serializable
data class ImmersiveImage(val productId: String) : NavigationKey

@NavigationDestination(ImmersiveImage::class)
val immersiveImageDestination: NavigationDestinationProvider<ImmersiveImage> = navigationDestination(
    metadata = { fullScreen() },
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "Immersive view: ${navigation.key.productId}",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
        )
        IconButton(
            onClick = { navigation.close() },
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
        }
    }
}
