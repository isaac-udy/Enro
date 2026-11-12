/**
 * Enro Recipe: OpenedTimestampPlugin
 *
 * Shows a NavigationPlugin that stores the time of opening in the metadata
 * of each NavigationKey.Instance. A destination reads the value from
 * `navigation.instance.metadata`.
 *
 * `RecipesComponent` registers the plugin, so the plugin receives `onOpened`
 * for every destination in the recipes app.
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

/**
 * The time in epoch milliseconds when the instance was first opened. The
 * default is `null`, which a destination reads when the plugin is not
 * registered.
 *
 * `internal` is required for Kotlin/Native ObjC export: the compiler marks
 * the generic `NavigationKey.MetadataKey` as `@HiddenFromObjC` and rejects a
 * public subtype of a `@HiddenFromObjC` type.
 */
internal object OpenedAt : NavigationKey.MetadataKey<Long?>(default = null)

class OpenedTimestampPlugin : NavigationPlugin() {
    override fun onOpened(navigationHandle: NavigationHandle<*>) {
        val instance = navigationHandle.instance
        // onOpened can run more than once for an instance, for example after
        // the instance is restored from saved state. The first value is kept.
        if (instance.metadata.get(OpenedAt) == null) {
            instance.metadata.set(OpenedAt, Clock.System.now().toEpochMilliseconds())
        }
    }
}

@Serializable
object OpenedTimestampPluginRecipe : NavigationKey

@Serializable
data object TimestampHome : NavigationKey

@Serializable
data class TimestampDetail(val label: String) : NavigationKey

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

@Composable
private fun OpenedTimestampPanel(
    navigation: NavigationHandle<*>,
    body: String,
    action: @Composable () -> Unit,
) {
    // The typed key returns the value that OpenedTimestampPlugin stored, or the
    // default `null`.
    val openedAt = navigation.instance.metadata.get(OpenedAt)

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
