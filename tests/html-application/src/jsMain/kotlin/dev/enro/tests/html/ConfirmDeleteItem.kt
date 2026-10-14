package dev.enro.tests.html

import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.complete
import dev.enro.html.htmlDialog
import dev.enro.requestClose
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestinationContent
import kotlinx.serialization.Serializable
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text

@Serializable
data class ConfirmDeleteItem(val item: String) : NavigationKey.WithResult<Boolean>

@NavigationDestination(ConfirmDeleteItem::class)
val confirmDeleteItemDestination: NavigationDestinationProvider<ConfirmDeleteItem> =
    navigationDestinationContent(metadata = { htmlDialog() }) {
        H2 { Text("Delete ${navigation.key.item}?") }
        Div({ classes("actions") }) {
            Button({
                classes("secondary")
                onClick { navigation.requestClose() }
            }) { Text("Cancel") }
            Button({ onClick { navigation.complete(true) } }) { Text("Delete") }
        }
    }
