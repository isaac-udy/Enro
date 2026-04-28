/**
 * Enro Recipe: Fragment Interop (simplified)
 *
 * Demonstrates that Enro supports Fragment-based destinations via the enro-compat
 * module. The full Fragment interop API uses the legacy `dev.enro.core` package
 * (see :tests:application's FragmentPresentation for a working example).
 *
 * This recipe shows the simpler "Fragment-in-Compose" pattern via AndroidView, plus
 * documentation of how Fragment destinations are typically declared. Wiring a
 * Fragment as a real navigation destination requires using the legacy compat APIs
 * and is best demonstrated by the test application.
 */
package dev.enro.recipes.interop

import android.widget.LinearLayout
import android.widget.TextView
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
import androidx.compose.ui.viewinterop.AndroidView
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
data object InteropHome : NavigationKey

@Serializable
data class InteropLegacyDetail(val itemId: String) : NavigationKey

@Composable
@NavigationDestination(FragmentInteropRecipe::class)
fun FragmentInteropRecipeScreen() {
    val navigation = navigationHandle<FragmentInteropRecipe>()
    RecipeScaffold(
        title = "Fragment Interop",
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Fragment Interop", style = MaterialTheme.typography.titleLarge)
        Text(
            "Enro supports Fragment destinations via the enro-compat module's legacy " +
                "dev.enro.core APIs. See :tests:application's FragmentPresentation.kt for " +
                "a working example using the legacy APIs.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Embedded Android View", style = MaterialTheme.typography.titleSmall)
                AndroidView(
                    factory = { context ->
                        LinearLayout(context).apply {
                            orientation = LinearLayout.VERTICAL
                            addView(TextView(context).apply {
                                text = "This is a legacy Android View embedded in Compose."
                            })
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Button(onClick = { navigation.open(InteropLegacyDetail("item-1")) }) {
            Text("Open Detail (Compose)")
        }
    }
}

@Composable
@NavigationDestination(InteropLegacyDetail::class)
fun InteropLegacyDetailDestination() {
    val navigation = navigationHandle<InteropLegacyDetail>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Detail: ${navigation.key.itemId}", style = MaterialTheme.typography.titleLarge)
        Button(onClick = { navigation.close() }) { Text("Back") }
    }
}
