package dev.enro.tests.html

import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.annotations.NavigationPath
import dev.enro.close
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestinationContent
import kotlinx.serialization.Serializable
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text

@Serializable
@NavigationPath("/items/{item}")
data class ShoppingListItem(val item: String) : NavigationKey

@NavigationDestination(ShoppingListItem::class)
val shoppingListItemDestination: NavigationDestinationProvider<ShoppingListItem> = navigationDestinationContent {
    H1 { Text(navigation.key.item) }
    Button({ onClick { navigation.close() } }) { Text("Back to the list") }
}
