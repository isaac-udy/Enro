package dev.enro.tests.html

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.annotations.NavigationPath
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestinationContent
import dev.enro.viewmodel.createEnroViewModel
import kotlinx.serialization.Serializable
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul

@Serializable
@NavigationPath("/")
object ShoppingList : NavigationKey

class ShoppingListViewModel : ViewModel() {
    private val navigation by navigationHandle<ShoppingList>()

    var items by mutableStateOf(listOf("Apples", "Bread", "Coffee"))
        private set

    private val confirmDelete by registerForNavigationResult<Boolean> { confirmed ->
        val item = (key as ConfirmDeleteItem).item
        if (confirmed) items = items - item
    }

    fun onItemSelected(item: String) {
        navigation.open(ShoppingListItem(item))
    }

    fun onDeleteSelected(item: String) {
        confirmDelete.open(ConfirmDeleteItem(item))
    }
}

@NavigationDestination.PlatformOverride(ShoppingList::class)
val shoppingListDestination: NavigationDestinationProvider<ShoppingList> = navigationDestinationContent {
    val viewModel = viewModel { createEnroViewModel { ShoppingListViewModel() } }
    H1 { Text("Shopping list") }
    if (viewModel.items.isEmpty()) {
        P { Text("Nothing left to buy.") }
    }
    Ul {
        viewModel.items.forEach { item ->
            Li {
                Button({ onClick { viewModel.onItemSelected(item) } }) { Text(item) }
                Button({
                    classes("secondary")
                    onClick { viewModel.onDeleteSelected(item) }
                }) { Text("Delete") }
            }
        }
    }
}
