// Snippets for docs/ghpages/docs/getting-started/basic-concepts.md
// A separate package, because a key can only have one destination.
package dev.enro.docs.gettingstarted.basicconcepts.provider

import androidx.compose.material3.Text
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.ui.navigationDestination
import kotlinx.serialization.Serializable

@Serializable
data class ShowProfile(val userId: String) : NavigationKey

// region docs:basic-concepts/provider-destination
@NavigationDestination(ShowProfile::class)
val profileDestination = navigationDestination<ShowProfile> {
    Text("Profile for ${navigation.key.userId}")
}
// endregion
