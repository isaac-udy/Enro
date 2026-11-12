/**
 * Enro Recipe: Fragment and Activity (Android)
 *
 * Demonstrates Fragments and Activities as destinations, in the same
 * navigation as Composable destinations.
 *
 * A Fragment or Activity becomes a destination the same way a Composable
 * does: `@NavigationDestination(Key::class)` on the class. Nothing else
 * registers it, and no extra Enro module is needed. Inside it,
 * `by navigationHandle<Key>()` gives the same NavigationHandle a Composable
 * gets, so `open`, `close` and `complete` all work.
 *
 * In this recipe:
 *  - a Composable opens a Fragment, which is shown in the same container
 *  - the Fragment opens a Composable
 *  - a Composable opens an Activity for a result, and the Activity returns
 *    one with `navigation.complete(...)`
 *
 * An Activity destination must also be declared in the app's
 * AndroidManifest.xml, like any Activity.
 *
 * Nav3 equivalent: the Fragment and View Interop recipe.
 */
package dev.enro.recipes.interop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button as AndroidButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object FragmentAndActivityRecipe : NavigationKey

@Serializable
data object InteropMenu : NavigationKey

/** Implemented by a Fragment. */
@Serializable
data class GreetingFragmentKey(val name: String) : NavigationKey

/** Implemented by a Composable, opened from the Fragment. */
@Serializable
data class GreetingDetails(val name: String) : NavigationKey

/** Implemented by an Activity that returns a result. */
@Serializable
data object PickNameActivityKey : NavigationKey.WithResult<String>

@Composable
@NavigationDestination(FragmentAndActivityRecipe::class)
fun FragmentAndActivityRecipeScreen() {
    val navigation = navigationHandle<FragmentAndActivityRecipe>()
    RecipeScaffold(
        title = "Fragment and Activity",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(InteropMenu.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(InteropMenu::class)
fun InteropMenuDestination() {
    val navigation = navigationHandle<InteropMenu>()
    var name by rememberSaveable { mutableStateOf("Compose") }
    // The Activity is opened like any other result-bearing destination.
    val pickName = registerForNavigationResult<String> { picked -> name = picked }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Fragment and Activity", style = MaterialTheme.typography.titleLarge)
        Text("Name: $name")
        Button(onClick = { navigation.open(GreetingFragmentKey(name)) }) {
            Text("Open a Fragment")
        }
        Button(onClick = { pickName.open(PickNameActivityKey) }) {
            Text("Pick a name in an Activity")
        }
    }
}

@NavigationDestination(GreetingFragmentKey::class)
class GreetingFragment : Fragment() {
    private val navigation by navigationHandle<GreetingFragmentKey>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val context = requireContext()
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            addView(TextView(context).apply {
                text = "Hello ${navigation.key.name}, from a Fragment"
                textSize = 20f
            })
            addView(AndroidButton(context).apply {
                text = "Open a Composable"
                setOnClickListener { navigation.open(GreetingDetails(navigation.key.name)) }
            })
            addView(AndroidButton(context).apply {
                text = "Close Fragment"
                setOnClickListener { navigation.close() }
            })
        }
    }
}

@Composable
@NavigationDestination(GreetingDetails::class)
fun GreetingDetailsDestination() {
    val navigation = navigationHandle<GreetingDetails>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("A Composable, opened from the Fragment", style = MaterialTheme.typography.titleLarge)
        Text("Name: ${navigation.key.name}")
        Button(onClick = { navigation.close() }) { Text("Back to the Fragment") }
    }
}

// region docs:android/activity-result
// Declared in the recipes app's AndroidManifest.xml.
@NavigationDestination(PickNameActivityKey::class)
class PickNameActivity : ComponentActivity() {
    private val navigation by navigationHandle<PickNameActivityKey>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Pick a name, in an Activity", style = MaterialTheme.typography.titleLarge)
                        listOf("Ada", "Grace").forEach { name ->
                            // Finishes the Activity and delivers the result to the caller.
                            Button(onClick = { navigation.complete(name) }) { Text(name) }
                        }
                        Button(onClick = { navigation.close() }) { Text("Cancel") }
                    }
                }
            }
        }
    }
}
// endregion
