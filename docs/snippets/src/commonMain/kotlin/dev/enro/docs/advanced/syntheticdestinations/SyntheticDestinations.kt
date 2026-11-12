// Snippets for docs/ghpages/docs/advanced/synthetic-destinations.md
package dev.enro.docs.advanced.syntheticdestinations

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.open
import dev.enro.path.getNavigationKeyFromPath
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.ui.destinations.completeFrom
import dev.enro.ui.destinations.syntheticDestination
import kotlinx.serialization.Serializable

@Serializable
data object Home : NavigationKey

@Serializable
data object Logout : NavigationKey

@Serializable
data object SettingsScreen : NavigationKey

@Serializable
data class ProfileUpdate(val name: String)

@Serializable
data class DeepLinkResolver(val url: String) : NavigationKey

private object featureFlags {
    val useNewProfileUi = true
}

fun callers(navigation: NavigationHandle<*>) {
    // region docs:synthetic-destinations/callers
    navigation.open(Logout)        // synthetic
    navigation.open(EditProfile)   // synthetic: opens one of two screens
    navigation.open(SettingsScreen) // ordinary destination
    // endregion
}

// region docs:synthetic-destinations/decider
@Serializable
object EditProfile : NavigationKey.WithResult<ProfileUpdate>

@Serializable
object EditProfileLegacy : NavigationKey.WithResult<ProfileUpdate>

@Serializable
object EditProfileV2 : NavigationKey.WithResult<ProfileUpdate>

@NavigationDestination(EditProfile::class)
val editProfile = syntheticDestination<EditProfile> {
    if (featureFlags.useNewProfileUi) completeFrom(EditProfileV2)
    else completeFrom(EditProfileLegacy)
}
// endregion

@Composable
fun DeciderCaller() {
    // region docs:synthetic-destinations/decider-caller
    val editProfile = registerForNavigationResult<ProfileUpdate>(
        onCompleted = { update -> /* runs for a result from either screen */ },
    )
    Button(onClick = { editProfile.open(EditProfile) }) { Text("Edit") }
    // endregion
}

// region docs:synthetic-destinations/side-effect-backstack
@NavigationDestination(DeepLinkResolver::class)
val deepLinkResolver = syntheticDestination<DeepLinkResolver> {
    sideEffect {
        val resolved = context.controller.getNavigationKeyFromPath(key.url)
            ?: return@sideEffect
        container.execute(
            context = context,
            operation = NavigationOperation.SetBackstack(
                currentBackstack = container.backstack,
                targetBackstack = backstackOf(Home.asInstance(), resolved.asInstance()),
            ),
        )
    }
}
// endregion
