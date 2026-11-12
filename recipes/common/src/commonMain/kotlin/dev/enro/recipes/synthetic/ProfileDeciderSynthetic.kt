/**
 * Enro Recipe: Synthetic Destination — Profile decider (`completeFrom`)
 *
 * Shows result forwarding with `syntheticDestination` and `completeFrom`.
 * `EditProfile` is a `NavigationKey.WithResult<ProfileUpdate>` with a
 * synthetic destination that selects one of two destinations at runtime.
 * The selected destination completes with a `ProfileUpdate`, and the caller
 * that opened `EditProfile` receives the `ProfileUpdate`.
 *
 * The recipe selects the destination at random. An app can select the
 * destination from a feature flag or remote config value.
 */
package dev.enro.recipes.synthetic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.recipes.RecipeScaffold
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.destinations.completeFrom
import dev.enro.ui.destinations.syntheticDestination
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
object ProfileDeciderSyntheticRecipe : NavigationKey

@Serializable
object ProfileDeciderHome : NavigationKey

@Serializable
data class ProfileUpdate(
    val displayName: String,
    val variant: String, // the screen that created the update: "legacy" or "v2"
)

/**
 * The key that a caller opens to edit the profile. The synthetic destination
 * for [EditProfile] forwards to [EditProfileLegacy] or [EditProfileV2].
 */
@Serializable
object EditProfile : NavigationKey.WithResult<ProfileUpdate>

@Serializable
object EditProfileLegacy : NavigationKey.WithResult<ProfileUpdate>

@Serializable
object EditProfileV2 : NavigationKey.WithResult<ProfileUpdate>

@NavigationDestination(EditProfile::class)
val editProfile = syntheticDestination<EditProfile> {
    val useV2 = Random.nextBoolean()
    // completeFrom opens the selected key. When the selected destination calls
    // navigation.complete(...), the caller that opened EditProfile receives
    // the result.
    if (useV2) completeFrom(EditProfileV2) else completeFrom(EditProfileLegacy)
}

@Composable
@NavigationDestination(ProfileDeciderSyntheticRecipe::class)
fun ProfileDeciderSyntheticRecipeScreen() {
    val navigation = navigationHandle<ProfileDeciderSyntheticRecipe>()
    RecipeScaffold(
        title = "Synthetic: Profile decider",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(ProfileDeciderHome.asInstance()),
        )
        NavigationDisplay(state = container, modifier = modifier)
    }
}

@Composable
@NavigationDestination(ProfileDeciderHome::class)
fun ProfileDeciderHomeDestination() {
    var lastResult by rememberSaveable { mutableStateOf<ProfileUpdate?>(null) }

    val editProfileRequest = registerForNavigationResult<ProfileUpdate>(
        onClosed = { /* the selected destination closed without a result */ },
        onCompleted = { result -> lastResult = result },
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Profile decider via a synthetic destination",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Tapping the button below opens `EditProfile` (a synthetic). The synthetic " +
                "picks between two real implementations — legacy and V2 — at random, then " +
                "`completeFrom`s the chosen one. The result of whichever screen actually ran " +
                "is forwarded back here.",
            style = MaterialTheme.typography.bodySmall,
        )

        Button(onClick = { editProfileRequest.open(EditProfile) }) {
            Text("Edit profile")
        }

        Text(
            text = "Last result:",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 16.dp),
        )
        val current = lastResult
        if (current == null) {
            Text(
                text = "(none yet — tap 'Edit profile' to try)",
                style = MaterialTheme.typography.bodySmall,
            )
        } else {
            Text("Display name: ${current.displayName}")
            Text("Came from: ${current.variant}")
        }
    }
}

@Composable
@NavigationDestination(EditProfileLegacy::class)
fun EditProfileLegacyDestination() {
    val navigation = navigationHandle<EditProfileLegacy>()
    var name by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "Edit profile (legacy UI)", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "This is the older edit-profile screen. The synthetic forwarded here on " +
                "this open.",
            style = MaterialTheme.typography.bodySmall,
        )

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Display name") },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                navigation.complete(ProfileUpdate(displayName = name, variant = "legacy"))
            },
            enabled = name.isNotBlank(),
        ) {
            Text("Save")
        }
        Button(onClick = { navigation.close() }) {
            Text("Cancel")
        }
    }
}

@Composable
@NavigationDestination(EditProfileV2::class)
fun EditProfileV2Destination() {
    val navigation = navigationHandle<EditProfileV2>()
    var name by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "Edit profile (V2)", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "This is the new edit-profile screen. Same contract as legacy, different " +
                "shape.",
            style = MaterialTheme.typography.bodySmall,
        )

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("What should we call you?") },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                navigation.complete(ProfileUpdate(displayName = name, variant = "v2"))
            },
            enabled = name.isNotBlank(),
        ) {
            Text("Save profile")
        }
        Button(onClick = { navigation.close() }) {
            Text("Discard")
        }
    }
}
