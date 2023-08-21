package dev.enro.example.destinations.result.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationKey
import dev.enro.core.closeWithResult
import dev.enro.destination.compose.dialog.BottomSheetDestination
import dev.enro.destination.compose.navigationHandle
import kotlinx.parcelize.Parcelize

@Parcelize
class GetString(
    val title: String = "", // In a real application, you should prefer to pass a String resource
    val buttonTitle: String = "Confirm",
) : NavigationKey.SupportsPresent.WithResult<String>, NavigationKey.SupportsPush.WithResult<String>

@Composable
@OptIn(ExperimentalMaterialApi::class)
@NavigationDestination(GetString::class)
fun GetStringDestination() {
    val direction = navigationHandle().instruction.navigationDirection

    when(direction) {
        is NavigationDirection.Present -> BottomSheetDestination { sheetState ->
            ModalBottomSheetLayout(
                sheetState = sheetState,
                sheetContent = { GetStringContent() }
            ) {}
        }
        else -> Box(
            modifier = Modifier.fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            GetStringContent()
        }
    }
}

@Composable
private fun GetStringContent() {
    val navigation = navigationHandle<GetString>()
    var input by remember {
        mutableStateOf("")
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = .5.dp)
            .padding(
                top = 32.dp,
                bottom = 32.dp
            )
    ) {
        Text(
            text = navigation.key.title,
            style = MaterialTheme.typography.h5,
        )
        OutlinedTextField(
            value = input,
            onValueChange = {
                input = it
            },
            label = {
                Text(navigation.key.title)
            }
        )
        OutlinedButton(
            onClick = {
                navigation.closeWithResult(input)
            }
        ) {
            Text(text = navigation.key.buttonTitle)
        }
    }
}