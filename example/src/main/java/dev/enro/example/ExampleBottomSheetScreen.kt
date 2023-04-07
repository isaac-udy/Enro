package dev.enro.example

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
import dev.enro.example.ui.ExampleScreenTemplate
import kotlinx.parcelize.Parcelize

@Parcelize
class ExampleComposableBottomSheetKey : NavigationKey.SupportsPresent

@OptIn(ExperimentalMaterialApi::class)
@Composable
@NavigationDestination(ExampleComposableBottomSheetKey::class)
fun ExampleBottomSheetScreen() = BottomSheetDestination { bottomSheetState ->
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