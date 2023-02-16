package dev.enro.example

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
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
fun BottomSheetDestination.ExampleBottomSheetScreen() {
    ExampleScreenTemplate(title = "Bottom Sheet")
}