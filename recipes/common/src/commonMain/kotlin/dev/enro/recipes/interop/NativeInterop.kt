/**
 * Enro Recipe: Native Interop
 *
 * Shows native (non-Compose) UI embedded in an Enro destination. Each platform
 * embeds a different native UI element:
 *
 * - Android: an `AndroidView` that hosts a `View`
 * - Desktop: a `SwingPanel`
 * - iOS: a `UIKitView` that hosts a `UILabel`
 * - WasmJs: Compose text in place of a native element
 *
 * Common code declares the destination. The `expect` function
 * [PlatformInteropContent] has an `actual` implementation on each platform.
 *
 * Nav3 equivalent: the Fragment Interop recipe, which is Android-only.
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
object NativeInteropRecipe : NavigationKey

@Serializable
data object InteropHome : NavigationKey

@Serializable
data class InteropDetail(val itemId: String) : NavigationKey

@Composable
@NavigationDestination(NativeInteropRecipe::class)
fun NativeInteropRecipeScreen() {
    val navigation = navigationHandle<NativeInteropRecipe>()
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

/** The native UI that [InteropHomeDestination] embeds. */
@Composable
expect fun PlatformInteropContent()
