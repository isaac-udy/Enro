/**
 * Enro Recipe: Handling a Deep Link
 *
 * Shows how to open a destination from a URL that the app receives from
 * outside, such as an Android intent.
 *
 * [openDeepLink] has two steps:
 *  1. `getNavigationKeyFromPath` resolves the path of the URL to a
 *     NavigationKey, using the path bindings registered on the
 *     NavigationComponent
 *  2. `updateBackstack` adds the key to the backstack of a container
 *
 * The recipes Android app calls [openDeepLink] on the root container from
 * `RecipesActivity` for `enrorecipes://app/...` links. To send a link:
 *
 *   adb shell am start -a android.intent.action.VIEW -d "enrorecipes://app/users/alice"
 *
 * The buttons on the recipe screen also call [openDeepLink], so the recipe
 * runs on platforms that do not send the app a URL.
 *
 * The Basic Deep Link recipe declares the path bindings. The Advanced Deep
 * Link recipe gives a deep link a backstack of parent screens.
 *
 * Nav3 equivalents: the Static Uri and Uri with Arguments recipes.
 */
package dev.enro.recipes.deeplink

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asBackstack
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.navigationHandle
import dev.enro.path.getNavigationKeyFromPath
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.LocalNavigationContainer
import dev.enro.ui.LocalNavigationContext
import dev.enro.ui.NavigationContainerState
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

/**
 * Adds the key bound to [path] to the top of this container's backstack.
 * Returns false and leaves the backstack unchanged when no path binding
 * matches [path].
 *
 * [path] is the path and query of the URL, for example `/users/alice` or
 * `/products/shoe-1?source=email`.
 */
// region docs:deep-links/open-deep-link
fun NavigationContainerState.openDeepLink(
    context: NavigationContext,
    path: String,
): Boolean {
    val key = context.getNavigationKeyFromPath(path) ?: return false
    updateBackstack { backstack -> (backstack + key.asInstance()).asBackstack() }
    return true
}
// endregion

@Serializable
object HandleDeepLinkRecipe : NavigationKey

@Serializable
data object DeepLinkInbox : NavigationKey

@Composable
@NavigationDestination(HandleDeepLinkRecipe::class)
fun HandleDeepLinkRecipeScreen() {
    val navigation = navigationHandle<HandleDeepLinkRecipe>()
    RecipeScaffold(
        title = "Handling a Deep Link",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(DeepLinkInbox.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(DeepLinkInbox::class)
fun DeepLinkInboxDestination() {
    val container = LocalNavigationContainer.current
    val context = LocalNavigationContext.current
    var unhandled by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Deep links", style = MaterialTheme.typography.titleLarge)
        Text(
            "On Android, send the app a link:\n" +
                "adb shell am start -a android.intent.action.VIEW -d \"enrorecipes://app/users/alice\"",
            style = MaterialTheme.typography.bodySmall,
        )
        Text("Or simulate a link arriving:", style = MaterialTheme.typography.titleSmall)

        listOf(
            "/users/alice",
            "/products/shoe-1?source=push",
            "/no-such-screen",
        ).forEach { path ->
            Button(onClick = {
                val handled = container.openDeepLink(context, path)
                unhandled = if (handled) null else path
            }) {
                Text("Receive $path")
            }
        }

        unhandled?.let {
            Text(
                "No destination for $it",
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
