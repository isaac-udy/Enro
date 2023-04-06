package dev.enro.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.core.compose.navigationHandle
import dev.enro.example.ui.ExampleScreenTemplate
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
class ExampleDialogComposableKey : NavigationKey.SupportsPresent

@Composable
@NavigationDestination(ExampleDialogComposableKey::class)
fun ExampleDialogComposable() {
    val navigation = navigationHandle()

    // Note: Dialogs do not save their instance state correctly when the saved state exists only in the dialog
    Dialog(onDismissRequest = { navigation.close() }) {
        ExampleScreenTemplate(title = "Dialog Composable", modifier = Modifier)
    }
}