/**
 * Enro Recipe: Koin ViewModel
 *
 * Shows a ViewModel that takes dependencies from Koin and arguments from the
 * NavigationKey of the destination.
 *
 * A ViewModel reads the key with `by navigationHandle<Key>()`. The ViewModel
 * constructor must run inside `createEnroViewModel { }`, which provides the
 * destination's NavigationHandle while the constructor runs. Koin's
 * `koinViewModel()` does not call `createEnroViewModel`, so
 * `by navigationHandle<Key>()` throws in a ViewModel created with
 * `koinViewModel()`.
 *
 * [koinEnroViewModel] gets the ViewModel from Koin inside
 * `createEnroViewModel { }`. The Koin module declares the ViewModel with
 * `factory { }`, because the AndroidX `viewModel()` call stores the ViewModel.
 *
 * The recipe creates an isolated Koin application, so the recipe does not
 * require the recipes app to start Koin. An app that calls `startKoin` at
 * startup does not require `KoinIsolatedContext`.
 *
 * Nav3 equivalent: the Koin injected ViewModel recipe.
 */
package dev.enro.recipes.koin

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
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import dev.enro.viewmodel.createEnroViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.KoinIsolatedContext
import org.koin.compose.getKoin
import org.koin.dsl.koinApplication
import org.koin.dsl.module

@Serializable
object KoinViewModelRecipe : NavigationKey

@Serializable
data object KoinUserList : NavigationKey

@Serializable
data class KoinUserDetail(val userId: String) : NavigationKey

class UserRepository {
    private val names = mapOf("ada" to "Ada Lovelace", "alan" to "Alan Turing")
    fun userIds(): List<String> = names.keys.toList()
    fun nameFor(userId: String): String = names[userId] ?: "Unknown user"
}

// region docs:di/koin-view-model
class KoinUserDetailViewModel(
    repository: UserRepository,
) : ViewModel() {
    // navigation.key is the key that opened the destination. The key
    // contains the arguments of the ViewModel.
    private val navigation by navigationHandle<KoinUserDetail>()

    val title: String = repository.nameFor(navigation.key.userId)

    fun onBackClicked() = navigation.close()
}

val koinRecipeModule = module {
    single { UserRepository() }
    // `factory`, not Koin's `viewModel`: the AndroidX viewModel() call stores the ViewModel.
    factory { KoinUserDetailViewModel(repository = get()) }
}
// endregion

private val recipeKoin = koinApplication { modules(koinRecipeModule) }

/**
 * Gets [T] from Koin inside `createEnroViewModel`, which provides the
 * NavigationHandle that `by navigationHandle()` reads in the ViewModel.
 */
// region docs:di/koin-helper
@Composable
inline fun <reified T : ViewModel> koinEnroViewModel(): T {
    val koin = getKoin()
    return viewModel<T> { createEnroViewModel { koin.get<T>() } }
}
// endregion

@Composable
@NavigationDestination(KoinViewModelRecipe::class)
fun KoinViewModelRecipeScreen() {
    val navigation = navigationHandle<KoinViewModelRecipe>()
    RecipeScaffold(
        title = "Koin ViewModel",
        navigation = navigation,
    ) { modifier ->
        KoinIsolatedContext(context = recipeKoin) {
            val container = rememberNavigationContainer(
                backstack = backstackOf(KoinUserList.asInstance()),
            )
            NavigationDisplay(
                state = container,
                modifier = modifier,
            )
        }
    }
}

@Composable
@NavigationDestination(KoinUserList::class)
fun KoinUserListDestination() {
    val navigation = navigationHandle<KoinUserList>()
    val repository = getKoin().get<UserRepository>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Users", style = MaterialTheme.typography.titleLarge)
        repository.userIds().forEach { userId ->
            Button(onClick = { navigation.open(KoinUserDetail(userId)) }) {
                Text("Open $userId")
            }
        }
    }
}

// region docs:di/koin-usage
@Composable
@NavigationDestination(KoinUserDetail::class)
fun KoinUserDetailDestination() {
    val viewModel = koinEnroViewModel<KoinUserDetailViewModel>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(viewModel.title, style = MaterialTheme.typography.titleLarge)
        Button(onClick = { viewModel.onBackClicked() }) { Text("Back") }
    }
}
// endregion
