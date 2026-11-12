/**
 * Enro Recipe: Koin ViewModel
 *
 * Demonstrates a ViewModel whose dependencies come from Koin and whose
 * arguments come from its NavigationKey.
 *
 * A ViewModel reads its key with `by navigationHandle<Key>()`. For that to
 * work, the ViewModel has to be constructed inside `createEnroViewModel { }`,
 * which makes the destination's NavigationHandle available while the
 * constructor runs. Koin's own `koinViewModel()` does not do this, so a
 * ViewModel created with it throws when it reads its NavigationHandle.
 *
 * [koinEnroViewModel] is the small bridge: it asks Koin for the instance
 * inside `createEnroViewModel { }`. The ViewModel is declared in the Koin
 * module with `factory { }`, because the AndroidX `viewModel()` call owns
 * its lifetime, not Koin.
 *
 * The recipe starts its own isolated Koin application so it does not depend
 * on the recipes app starting Koin. A real app would call `startKoin` once
 * at startup and would not need `KoinIsolatedContext`.
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

// A dependency provided by Koin.
class UserRepository {
    private val names = mapOf("ada" to "Ada Lovelace", "alan" to "Alan Turing")
    fun userIds(): List<String> = names.keys.toList()
    fun nameFor(userId: String): String = names[userId] ?: "Unknown user"
}

class KoinUserDetailViewModel(
    repository: UserRepository,
) : ViewModel() {
    // The key this destination was opened with: the ViewModel's arguments.
    private val navigation by navigationHandle<KoinUserDetail>()

    val title: String = repository.nameFor(navigation.key.userId)

    fun onBackClicked() = navigation.close()
}

val koinRecipeModule = module {
    single { UserRepository() }
    // factory, not viewModel: the AndroidX viewModel() call stores the instance.
    factory { KoinUserDetailViewModel(repository = get()) }
}

private val recipeKoin = koinApplication { modules(koinRecipeModule) }

/**
 * Creates [T] with Koin, inside `createEnroViewModel` so that
 * `by navigationHandle()` works in the ViewModel.
 */
@Composable
inline fun <reified T : ViewModel> koinEnroViewModel(): T {
    val koin = getKoin()
    return viewModel<T> { createEnroViewModel { koin.get<T>() } }
}

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
