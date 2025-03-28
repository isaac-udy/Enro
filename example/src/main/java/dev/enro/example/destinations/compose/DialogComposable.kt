package dev.enro.example.destinations.compose

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.compose.navigationHandle
import dev.enro.example.core.ui.ExampleScreenTemplate
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
class DialogComposable : Parcelable, NavigationKey.SupportsPresent

@Composable
@NavigationDestination(DialogComposable::class)
fun DialogComposableDestination() = DialogDestination {
    val navigation = navigationHandle()

    Dialog(onDismissRequest = { navigation.close() }) {
        ExampleScreenTemplate(title = "Dialog Composable", modifier = Modifier)
    }
}