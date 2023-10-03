package dev.enro.tests.application.compose

import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.dialog.configureBottomSheet
import dev.enro.core.compose.navigationHandle
import dev.enro.core.present
import dev.enro.core.requestClose
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.parcelize.Parcelize

@Parcelize
object LegacyBottomSheetDestination : NavigationKey.SupportsPush {
    @Parcelize
    internal class BottomSheet(
        val skipHalfExpanded: Boolean = false,
    ) : NavigationKey.SupportsPresent {
    }
}

@NavigationDestination(LegacyBottomSheetDestination::class)
@Composable
fun LegacyBottomSheetDestination() {
    val navigationHandle = navigationHandle()

    TitledColumn("Legacy BottomSheet") {
        Button(onClick = {
            navigationHandle.present(
                LegacyBottomSheetDestination.BottomSheet()
            )
        }) {
            Text(text = "Bottom Sheet")
        }

        Button(onClick = {
            navigationHandle.present(
                LegacyBottomSheetDestination.BottomSheet(skipHalfExpanded = true)
            )
        }) {
            Text(text = "Bottom Sheet (Skip Half Expanded)")
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@NavigationDestination(LegacyBottomSheetDestination.BottomSheet::class)
@Composable
fun BottomSheetDestination.LegacyBottomSheetBottomSheet() {
    val navigationHandle = navigationHandle<LegacyBottomSheetDestination.BottomSheet>()
    val key = navigationHandle.key

    configureBottomSheet {
        setSkipHalfExpanded(key.skipHalfExpanded)
    }

    TitledColumn("Legacy BottomSheet") {
        Button(onClick = {
            navigationHandle.requestClose()
        }) {
            Text(text = "Close")
        }
    }
}