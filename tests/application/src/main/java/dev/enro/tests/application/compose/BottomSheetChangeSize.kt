package dev.enro.tests.application.compose

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.destination.compose.dialog.BottomSheetDestination
import dev.enro.destination.compose.navigationHandle
import dev.enro.core.present
import kotlinx.coroutines.delay
import kotlinx.parcelize.Parcelize

@Parcelize
object BottomSheetChangeSize : NavigationKey.SupportsPush {
    @Parcelize
    internal object BottomSheet : NavigationKey.SupportsPresent
}

@Composable
@NavigationDestination(BottomSheetChangeSize::class)
fun BottomSheetChangeSizeScreen() {
    val navigation = navigationHandle()
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(16.dp)
    ) {
        Text(text = "BottomSheet Changes Size", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navigation.present(BottomSheetChangeSize.BottomSheet)
        }) {
            Text(text = "Open BottomSheet")
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@NavigationDestination(BottomSheetChangeSize.BottomSheet::class)
fun BottomSheetChangeSizeBottomSheetScreen() = BottomSheetDestination { state ->
    var items by remember {
        mutableStateOf(emptyList<Int>())
    }

    ModalBottomSheetLayout(
        sheetState = state,
        sheetContent = {
            Column(
                Modifier
                    .background(MaterialTheme.colors.background)
                    .padding(16.dp)
            ) {
                Text(text = "BottomSheet")
                Spacer(modifier = Modifier.height(16.dp))
                items.forEach {
                    Text("Item $it")
                }
            }
        }
    ) {}

    LaunchedEffect(items.size) {
        delay(1)
        if(items.size < 100) {
            items = items + items.size
        }
    }
}