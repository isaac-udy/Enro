/**
 * Enro Recipe: Metro Modular
 *
 * Demonstrates features that contribute to one Metro graph and navigate to
 * each other by key. The packages stand in for Gradle modules:
 *
 *  - `modular` (this file) is the app. It declares the graph and the keys.
 *  - `modular.orders` and `modular.account` are features. Neither refers to
 *    the other.
 *
 * Each feature declares an interface with the accessors it needs and
 * contributes it to `AppScope` with `@ContributesTo`. The app's graph is
 * declared with `@DependencyGraph(AppScope::class)`, so Metro makes it
 * implement every contributed interface. A feature gets the graph from
 * [LocalMetroGraph] and casts it to its own interface.
 *
 * [MetroSession] is scoped to the graph, so both features get one instance:
 * a name saved on the account screen is shown on the orders screen.
 *
 * In a real app the keys would be in a navigation module, as in the Modular
 * Navigation recipe, and each feature would be a Gradle module that applies
 * the Metro plugin and the `enro-processor`.
 *
 * Nav3 equivalent: the Metro modular recipe.
 */
package dev.enro.recipes.metro.modular

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import dev.enro.viewmodel.createEnroViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraph
import kotlinx.serialization.Serializable

// The keys both features use.
@Serializable
data object MetroOrders : NavigationKey

@Serializable
data object MetroAccount : NavigationKey

// One instance for the whole graph.
@Inject
@SingleIn(AppScope::class)
class MetroSession {
    var userName: String by mutableStateOf("Guest")
}

// region docs:di/metro-modular-graph
// Implements every interface contributed to AppScope.
@DependencyGraph(AppScope::class)
interface MetroModularGraph

val LocalMetroGraph = staticCompositionLocalOf<Any> {
    error("No Metro graph was provided")
}

/**
 * Creates [T] from the feature's graph interface [G], inside
 * `createEnroViewModel` so that `by navigationHandle()` works in the ViewModel.
 */
@Composable
inline fun <reified G : Any, reified T : ViewModel> metroEnroViewModel(
    crossinline create: (G) -> T,
): T {
    val graph = LocalMetroGraph.current as G
    return viewModel<T> { createEnroViewModel { create(graph) } }
}
// endregion

/** The content of the Metro Modular recipe. */
@Composable
fun MetroModularRecipeContent(modifier: Modifier = Modifier) {
    // A real app creates its graph once at startup.
    val graph = remember { createGraph<MetroModularGraph>() }
    CompositionLocalProvider(LocalMetroGraph provides graph) {
        val container = rememberNavigationContainer(
            backstack = backstackOf(MetroOrders.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}
