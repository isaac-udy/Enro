/**
 * Enro Recipe: Synthetic Destination — Profile decider (`completeFrom`)
 *
 * Demonstrates the result-forwarding pattern: a synthetic with a result
 * contract (`NavigationKey.WithResult<ProfileUpdate>`) that picks between
 * several actual destinations at runtime. The chosen destination produces
 * the result and the synthetic dispatcher forwards it back to whoever
 * originally opened `EditProfile`.
 *
 * The decision here is a coin flip. In a real app this would read a
 * feature flag, an A/B test bucket, a remote config value, etc.
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
    val variant: String, // which UI produced the update — for the recipe to display
)

/**
 * Public-facing key that callers open when they want to edit the profile.
 * It's a synthetic; the dispatcher forwards to one of the concrete
 * implementations below based on a runtime decision.
 */
@Serializable
object EditProfile : NavigationKey.WithResult<ProfileUpdate>

/** Concrete implementation: the legacy edit-profile UI. */
@Serializable
object EditProfileLegacy : NavigationKey.WithResult<ProfileUpdate>

/** Concrete implementation: the new edit-profile UI. */
@Serializable
object EditProfileV2 : NavigationKey.WithResult<ProfileUpdate>

@NavigationDestination(EditProfile::class)
val editProfile = syntheticDestination<EditProfile> {
    // In a real app this is where you would read a feature flag, query an
    // A/B bucket, check remote config, etc. We toss a coin for the recipe.
    val useV2 = Random.nextBoolean()
    if (useV2) completeFrom(EditProfileV2) else completeFrom(EditProfileLegacy)
    // completeFrom opens the chosen key with EditProfile's result-channel
    // metadata copied across, so when that key calls navigation.complete(...)
    // the result routes back to whoever opened `EditProfile` in the first
    // place — they never need to know about the legacy/V2 split.
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
        onClosed = { /* user cancelled */ },
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
