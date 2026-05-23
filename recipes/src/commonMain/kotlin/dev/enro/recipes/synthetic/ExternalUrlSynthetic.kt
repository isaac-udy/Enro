/**
 * Enro Recipe: Synthetic Destination — External URL launcher
 *
 * Demonstrates the side-effect-bridge pattern: a `NavigationKey` whose
 * destination doesn't render any UI and doesn't change the Enro navigation
 * state. The synthetic block runs imperative work and then falls through;
 * the dispatcher treats fall-through as a silent close (no result-channel
 * callback fires).
 *
 * In a real app this synthetic would invoke a platform-specific API:
 *  - Android: `activity.startActivity(Intent(ACTION_VIEW, Uri.parse(url)))`
 *    or Chrome Custom Tabs via androidx.browser
 *  - Desktop:  `java.awt.Desktop.getDesktop().browse(URI(url))`
 *  - Web:     `window.open(url, "_blank")`
 *
 * Cross-platform launch from common code is out of scope for the recipe
 * itself, so the synthetic just records each requested URL in an in-memory
 * log that the recipe screen displays. The shape of the synthetic is what
 * matters here — the block runs, no Enro navigation happens.
 */
package dev.enro.recipes.synthetic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.destinations.syntheticDestination
import kotlinx.serialization.Serializable

@Serializable
object ExternalUrlSyntheticRecipe : NavigationKey

@Serializable
data class OpenExternalUrl(val url: String) : NavigationKey

// In a real app the synthetic would call into a platform API; here we
// keep a simple in-memory log so the recipe screen has something to show.
private val openedUrls = mutableStateListOf<String>()

@NavigationDestination(OpenExternalUrl::class)
val openExternalUrl = syntheticDestination<OpenExternalUrl> {
    // In a real app: launch CCT on Android, Desktop.browse on desktop,
    // window.open on web. See the file header for snippets.
    openedUrls.add(key.url)
    // No outcome method is called — the block falls through. The dispatcher
    // treats that as a silent close: no result-channel callback fires for
    // the caller, the synthetic instance never lands on any backstack, and
    // the Enro state is unchanged.
}

@Composable
@NavigationDestination(ExternalUrlSyntheticRecipe::class)
fun ExternalUrlSyntheticRecipeScreen() {
    val navigation = navigationHandle<ExternalUrlSyntheticRecipe>()
    RecipeScaffold(
        title = "Synthetic: External URL",
        navigation = navigation,
    ) { modifier ->
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Open external URLs as a navigation action.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Each button below calls navigation.open(OpenExternalUrl(...)). The " +
                    "synthetic 'opens' the URL and falls through — your Enro backstack " +
                    "doesn't change.",
                style = MaterialTheme.typography.bodySmall,
            )

            Button(onClick = { navigation.open(OpenExternalUrl("https://enro.dev")) }) {
                Text("Open enro.dev")
            }
            Button(onClick = { navigation.open(OpenExternalUrl("https://kotlinlang.org")) }) {
                Text("Open kotlinlang.org")
            }

            Text(
                text = "URLs the synthetic has 'opened' this session:",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 16.dp),
            )
            if (openedUrls.isEmpty()) {
                Text(
                    text = "(none yet — tap a button above)",
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                openedUrls.toList().asReversed().forEach { url ->
                    Text(text = "- $url", style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = { openedUrls.clear() }) {
                    Text("Clear log")
                }
            }
        }
    }
}
