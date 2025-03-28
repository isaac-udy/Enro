package dev.enro.tests.application.compose

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.navigationHandle
import dev.enro.core.present
import kotlinx.parcelize.Parcelize

@Parcelize
object BottomSheetCloseAndPresent : Parcelable, NavigationKey.SupportsPush {
    @Parcelize
    internal object BottomSheet : Parcelable, NavigationKey.SupportsPresent
}

@Composable
@NavigationDestination(BottomSheetCloseAndPresent::class)
fun CloseBottomSheetAndPresentScreen() {
    val navigation = navigationHandle()
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(16.dp)
    ) {
        Text(text = "BottomSheet Closes and Presents BottomSheet", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navigation.present(BottomSheetCloseAndPresent.BottomSheet)
        }) {
            Text(text = "Open BottomSheet")
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@NavigationDestination(BottomSheetCloseAndPresent.BottomSheet::class)
fun CloseBottomSheetAndPresentBottomSheetScreen() = BottomSheetDestination { state ->
    val navigation = navigationHandle()
    ModalBottomSheetLayout(
        sheetState = state,
        sheetContent = {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
                    .padding(16.dp)
            ) {
                Text(text = "BottomSheet")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    navigation.present(BottomSheetCloseAndPresent.BottomSheet)
                    navigation.close()
                }) {
                    Text(text = "Close and Present BottomSheet")
                }
            }
        }
    ) {}
}