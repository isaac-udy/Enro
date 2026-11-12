/**
 * Enro Recipe: Metro ViewModel
 *
 * Demonstrates a ViewModel whose dependencies come from a Metro dependency
 * graph and whose arguments come from its NavigationKey.
 *
 * A ViewModel reads the NavigationKey with `by navigationHandle<Key>()`.
 * `by navigationHandle` requires the ViewModel to be constructed inside
 * `createEnroViewModel { }`, which provides the destination's
 * NavigationHandle while the constructor runs.
 *
 * The graph exposes the ViewModel as an accessor. The ViewModel has no scope,
 * so each read of the accessor constructs a new instance; the AndroidX
 * `viewModel()` call stores the instance. The destination reads the accessor
 * inside `createEnroViewModel { }`.
 *
 * The recipe's host screen is in `:recipes:common`
 * (`metro/MetroRecipes.kt`); this module applies the Metro compiler plugin.
 *
 * Nav3 equivalent: the Metro injected ViewModel recipe.
 */
package dev.enro.recipes.metro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import dev.enro.viewmodel.createEnroViewModel
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.createGraph
import kotlinx.serialization.Serializable

@Serializable
data object MetroUserList : NavigationKey

@Serializable
data class MetroUserDetail(val userId: String) : NavigationKey

// A dependency provided by the graph.
@Inject
class MetroUserRepository {
    private val names = mapOf("ada" to "Ada Lovelace", "alan" to "Alan Turing")
    fun userIds(): List<String> = names.keys.toList()
    fun nameFor(userId: String): String = names[userId] ?: "Unknown user"
}

// region docs:di/metro-view-model
@Inject
class MetroUserDetailViewModel(
    repository: MetroUserRepository,
) : ViewModel() {
    // The key this destination was opened with: the ViewModel's arguments.
    private val navigation by navigationHandle<MetroUserDetail>()

    val title: String = repository.nameFor(navigation.key.userId)

    fun onBackClicked() = navigation.close()
}

@DependencyGraph
interface MetroRecipeGraph {
    val userRepository: MetroUserRepository

    // Not scoped: each read constructs a new ViewModel.
    val userDetailViewModel: MetroUserDetailViewModel
}
// endregion

// A real app creates its graph once at startup.
private val recipeGraph: MetroRecipeGraph by lazy { createGraph<MetroRecipeGraph>() }

@Composable
fun MetroViewModelRecipeContent(modifier: Modifier = Modifier) {
    val container = rememberNavigationContainer(
        backstack = backstackOf(MetroUserList.asInstance()),
    )
    NavigationDisplay(
        state = container,
        modifier = modifier,
    )
}

@Composable
@NavigationDestination(MetroUserList::class)
fun MetroUserListDestination() {
    val navigation = navigationHandle<MetroUserList>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Users", style = MaterialTheme.typography.titleLarge)
        recipeGraph.userRepository.userIds().forEach { userId ->
            Button(onClick = { navigation.open(MetroUserDetail(userId)) }) {
                Text("Open $userId")
            }
        }
    }
}

// region docs:di/metro-usage
@Composable
@NavigationDestination(MetroUserDetail::class)
fun MetroUserDetailDestination() {
    val viewModel = viewModel<MetroUserDetailViewModel> {
        createEnroViewModel { recipeGraph.userDetailViewModel }
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(viewModel.title, style = MaterialTheme.typography.titleLarge)
        Button(onClick = { viewModel.onBackClicked() }) { Text("Back") }
    }
}
// endregion
