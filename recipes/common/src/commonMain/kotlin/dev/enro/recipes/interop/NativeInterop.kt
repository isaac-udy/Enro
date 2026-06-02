/**
 * Enro Recipe: Native Interop
 *
 * Demonstrates embedding native (non-Compose) UI inside an Enro navigation destination.
 * The Nav3 equivalent ("Fragment Interop") is Android-specific; this recipe generalises
 * the idea so each platform shows how to integrate its own native UI primitive:
 *
 * - Android: an `AndroidView` hosting a classic `View` (path to Fragments via enro-compat)
 * - Desktop: a `SwingPanel` (when available) or a plain Compose placeholder
 * - iOS / WasmJs: a Compose placeholder describing the platform's interop story
 *
 * The destination itself is declared in common code; the per-platform UI is supplied via
 * an `expect`/`actual` function so the recipe is reachable on every Compose target.
 */
package dev.enro.recipes.interop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object FragmentInteropRecipe : NavigationKey

@Serializable
data object InteropHome : NavigationKey

@Serializable
data class InteropDetail(val itemId: String) : NavigationKey

@Composable
@NavigationDestination(FragmentInteropRecipe::class)
fun FragmentInteropRecipeScreen() {
    val navigation = navigationHandle<FragmentInteropRecipe>()
    RecipeScaffold(
        title = "Native Interop",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(InteropHome.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(InteropHome::class)
fun InteropHomeDestination() {
    val navigation = navigationHandle<InteropHome>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Native Interop", style = MaterialTheme.typography.titleLarge)
        Text(
            "Enro destinations can embed native UI alongside Compose content. Below " +
                "is a platform-specific embed; the rest of the destination uses standard " +
                "Compose APIs.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Native embed", style = MaterialTheme.typography.titleSmall)
                PlatformInteropContent()
            }
        }

        Button(onClick = { navigation.open(InteropDetail("item-1")) }) {
            Text("Open Detail (Compose)")
        }
    }
}

@Composable
@NavigationDestination(InteropDetail::class)
fun InteropDetailDestination() {
    val navigation = navigationHandle<InteropDetail>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Detail: ${navigation.key.itemId}", style = MaterialTheme.typography.titleLarge)
        Button(onClick = { navigation.close() }) { Text("Back") }
    }
}

/**
 * Platform-specific native UI embedded inside the recipe. Each target supplies its own
 * `actual` implementation.
 */
@Composable
expect fun PlatformInteropContent()
