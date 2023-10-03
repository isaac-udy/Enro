package dev.enro.tests.application.compose

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.compose.navigationHandle
import dev.enro.core.present
import dev.enro.core.requestClose
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.parcelize.Parcelize

@Parcelize
object LegacyDialogDestination : NavigationKey.SupportsPush {
    @Parcelize
    internal object Dialog : NavigationKey.SupportsPresent
}

@NavigationDestination(LegacyDialogDestination::class)
@Composable
fun LegacyDialogDestination() {
    val navigationHandle = navigationHandle()
    TitledColumn(
        "Legacy Dialog"
    ) {
        Button(onClick = {
            navigationHandle.present(LegacyDialogDestination.Dialog)
        }) {
            Text(text = "Dialog")
        }
    }
}

@NavigationDestination(LegacyDialogDestination.Dialog::class)
@Composable
fun DialogDestination.LegacyDialogDialog() {
    val navigationHandle = navigationHandle()
    Dialog(onDismissRequest = { navigationHandle.requestClose() }) {
        TitledColumn(
            "Legacy Dialog"
        ) {
            Button(onClick = {
                navigationHandle.requestClose()
            }) {
                Text(text = "Close")
            }
        }
    }
}