/**
 * Enro Recipe: OpenedTimestampPlugin
 *
 * Demonstrates a small NavigationPlugin that stamps every NavigationKey.Instance
 * with the wall-clock time at which it was opened. This is a canonical example
 * of "NavigationKey.Instance metadata" — extra data attached to an instance
 * that any destination might want to read, but that doesn't change how the
 * destination is rendered.
 *
 * Real-world uses for this pattern include analytics correlation, screen-time
 * tracking, and origin tagging for animations.
 *
 * The plugin is installed globally in RecipesComponent so every destination in
 * the recipes app receives a timestamp.
 */
package dev.enro.recipes.plugins

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.*
import dev.enro.annotations.NavigationDestination
import dev.enro.plugin.NavigationPlugin
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlin.time.Clock

// -- The metadata key --

/**
 * Epoch-millisecond timestamp captured the first time an instance is opened.
 * Defaults to null so the absence of the value is observable (for example,
 * the plugin might not be installed in tests).
 *
 * `internal` is required for K/Native's ObjC export — NavigationKey
 * .MetadataKey is a generic type and gets auto-marked @HiddenFromObjC by
 * the compiler, and public subtypes of a HiddenFromObjC type are rejected.
 */
internal object OpenedAt : NavigationKey.MetadataKey<Long?>(default = null)

// -- The plugin --

class OpenedTimestampPlugin : NavigationPlugin() {
    override fun onOpened(navigationHandle: NavigationHandle<*>) {
        val instance = navigationHandle.instance
        // Only stamp the first time we see this instance; subsequent opens (e.g.
        // an instance restored from saved state) keep their original timestamp.
        if (instance.metadata.get(OpenedAt) == null) {
            instance.metadata.set(OpenedAt, Clock.System.now().toEpochMilliseconds())
        }
    }
}

// -- Recipe keys --

@Serializable
object OpenedTimestampPluginRecipe : NavigationKey

@Serializable
data object TimestampHome : NavigationKey

@Serializable
data class TimestampDetail(val label: String) : NavigationKey

// -- Recipe root --

@Composable
@NavigationDestination(OpenedTimestampPluginRecipe::class)
fun OpenedTimestampPluginRecipeScreen() {
    val navigation = navigationHandle<OpenedTimestampPluginRecipe>()
    RecipeScaffold(
        title = "Opened Timestamp Plugin",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(TimestampHome.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(TimestampHome::class)
fun TimestampHomeDestination() {
    val navigation = navigationHandle<TimestampHome>()
    OpenedTimestampPanel(
        navigation = navigation,
        body = "This screen was opened at the timestamp below. The value is " +
            "set by OpenedTimestampPlugin, which is installed once in " +
            "RecipesComponent and applies to every navigation key in the app.",
        action = {
            Button(onClick = { navigation.open(TimestampDetail("Detail")) }) {
                Text("Open another screen")
            }
        },
    )
}

@Composable
@NavigationDestination(TimestampDetail::class)
fun TimestampDetailDestination() {
    val navigation = navigationHandle<TimestampDetail>()
    OpenedTimestampPanel(
        navigation = navigation,
        body = "Each destination gets its own timestamp at the moment it's " +
            "first opened. Press back to return to the previous screen — " +
            "its timestamp is unchanged.",
        action = {
            Button(onClick = { navigation.close() }) { Text("Back") }
        },
    )
}

// -- Shared UI: shows the OpenedAt metadata for the current destination --

@Composable
private fun OpenedTimestampPanel(
    navigation: NavigationHandle<*>,
    body: String,
    action: @Composable () -> Unit,
) {
    val openedAt = navigation.instance.metadata.get(OpenedAt)

    // Tick once per second so the "seconds since opened" display stays current.
    var now by remember { mutableLongStateOf(Clock.System.now().toEpochMilliseconds()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = Clock.System.now().toEpochMilliseconds()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(body)

        when (openedAt) {
            null -> Text("No OpenedAt metadata — is the plugin installed?")
            else -> {
                Text("Opened at: $openedAt (epoch ms)")
                Text("Seconds since opened: ${(now - openedAt) / 1000}")
            }
        }

        action()
    }
}
