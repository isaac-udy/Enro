/**
 * Enro Recipe: Shared ViewModel
 *
 * Shows one ViewModel shared by several destinations.
 *
 * The parent destination (`CheckoutHost`) creates the ViewModel and hosts a
 * nested container. Each destination inside the nested container gets the
 * ViewModel from the ViewModelStore of `CheckoutHost`, so every destination
 * reads the same instance. The ViewModel is cleared when `CheckoutHost`
 * leaves the backstack.
 *
 * The `NavigationContext` of a destination is a `DestinationContext`. The
 * `parent` of a `DestinationContext` is the `ContainerContext` of the
 * container that displays the destination. A `ContainerContext` is a
 * `ViewModelStoreOwner` that delegates to the destination that hosts the
 * container. A nested destination passes the `ContainerContext` as
 * `viewModelStoreOwner`.
 *
 * The Sharing State with Results recipe shares a value without a shared
 * ViewModel.
 *
 * Nav3 equivalent: the Shared ViewModel recipe.
 */
package dev.enro.recipes.viewmodel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.context.DestinationContext
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.LocalNavigationContext
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable

@Serializable
object SharedViewModelRecipe : NavigationKey

@Serializable
data object CheckoutHost : NavigationKey

@Serializable
data object CheckoutCart : NavigationKey

@Serializable
data object CheckoutReview : NavigationKey

/** [CheckoutHost] and each destination inside [CheckoutHost] read one instance. */
class CheckoutViewModel : ViewModel() {
    private val _itemCount = MutableStateFlow(1)
    val itemCount: StateFlow<Int> = _itemCount.asStateFlow()

    fun addItem() = _itemCount.update { it + 1 }
    fun removeItem() = _itemCount.update { (it - 1).coerceAtLeast(0) }
}

/**
 * Returns the ViewModelStoreOwner of the destination that hosts the
 * container of the calling destination.
 */
// region docs:contexts/parent-owner
@Composable
private fun hostViewModelStoreOwner(): ViewModelStoreOwner {
    val context = LocalNavigationContext.current as DestinationContext<*>
    return context.parent
}
// endregion

@Composable
@NavigationDestination(SharedViewModelRecipe::class)
fun SharedViewModelRecipeScreen() {
    val navigation = navigationHandle<SharedViewModelRecipe>()
    RecipeScaffold(
        title = "Shared ViewModel",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(CheckoutHost.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(CheckoutHost::class)
fun CheckoutHostDestination() {
    // The default ViewModelStoreOwner is the context of this destination, so
    // the ViewModelStore of CheckoutHost stores the ViewModel.
    val viewModel = viewModel<CheckoutViewModel> { CheckoutViewModel() }
    val itemCount by viewModel.itemCount.collectAsState()

    val container = rememberNavigationContainer(
        backstack = backstackOf(CheckoutCart.asInstance()),
    )
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Host sees $itemCount item(s)",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(16.dp),
        )
        NavigationDisplay(
            state = container,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
@NavigationDestination(CheckoutCart::class)
fun CheckoutCartDestination() {
    val navigation = navigationHandle<CheckoutCart>()
    // hostViewModelStoreOwner() returns the owner that CheckoutHost used, so
    // viewModel() returns the instance that CheckoutHost created.
    val viewModel = viewModel<CheckoutViewModel>(
        viewModelStoreOwner = hostViewModelStoreOwner(),
    ) { CheckoutViewModel() }
    val itemCount by viewModel.itemCount.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Cart", style = MaterialTheme.typography.titleLarge)
        Text("$itemCount item(s)")
        Button(onClick = { viewModel.addItem() }) { Text("Add an item") }
        Button(onClick = { viewModel.removeItem() }) { Text("Remove an item") }
        Button(onClick = { navigation.open(CheckoutReview) }) { Text("Review order") }
    }
}

@Composable
@NavigationDestination(CheckoutReview::class)
fun CheckoutReviewDestination() {
    val navigation = navigationHandle<CheckoutReview>()
    val viewModel = viewModel<CheckoutViewModel>(
        viewModelStoreOwner = hostViewModelStoreOwner(),
    ) { CheckoutViewModel() }
    val itemCount by viewModel.itemCount.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Review", style = MaterialTheme.typography.titleLarge)
        Text("Ordering $itemCount item(s)")
        Button(onClick = { viewModel.addItem() }) { Text("Add one more") }
        Button(onClick = { navigation.close() }) { Text("Back to cart") }
    }
}
