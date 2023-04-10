package dev.enro.example.destinations.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.example.core.ui.ExampleScreenTemplate
import kotlinx.parcelize.Parcelize

@Parcelize
class BottomSheetComposable : NavigationKey.SupportsPresent

@OptIn(ExperimentalMaterialApi::class)
@Composable
@NavigationDestination(BottomSheetComposable::class)
fun BottomSheetScreen() = BottomSheetDestination { bottomSheetState ->
    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = .5.dp)
            ) {
                ExampleScreenTemplate(title = "Bottom Sheet")
            }
        },
        content = {}
    )
}