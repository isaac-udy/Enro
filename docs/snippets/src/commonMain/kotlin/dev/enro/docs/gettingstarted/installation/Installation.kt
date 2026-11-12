// Snippets for docs/ghpages/docs/getting-started/installation.md
package dev.enro.docs.gettingstarted.installation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationComponent
import dev.enro.annotations.NavigationDestination
import dev.enro.controller.NavigationComponentConfiguration
import dev.enro.controller.createNavigationModule
import kotlinx.serialization.Serializable

@Serializable
data object Home : NavigationKey

@Composable
@NavigationDestination(Home::class)
fun HomeScreen() {
    Text("Home")
}

// region docs:installation/component
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        // Optional: plugins, interceptors, decorators, serializers,
        // and modules from other libraries.
    }
)
// endregion
