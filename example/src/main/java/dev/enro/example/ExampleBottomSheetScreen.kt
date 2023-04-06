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
import dev.enro.core.compose.navigationHandle
import dev.enro.core.container.setBackstack
import dev.enro.example.ui.ExampleScreenTemplate
import kotlinx.parcelize.Parcelize

@Parcelize
class ExampleComposableBottomSheetKey : NavigationKey.SupportsPresent

//@OptIn(ExperimentalMaterialApi::class)
//@Composable
//@NavigationDestination(ExampleComposableBottomSheetKey::class)
//fun BottomSheetDestination.ExampleBottomSheetScreen() {
//    ExampleScreenTemplate(title = "Bottom Sheet")
//}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@NavigationDestination(ExampleComposableBottomSheetKey::class)
fun ExampleBottomSheetScreen() {
    val navigationHandle = navigationHandle()
    val parent = parentContainer
    val isDismissed by derivedStateOf {
        parent?.backstack?.none { it.instructionId == navigationHandle.instruction.instructionId } ?: false
    }
    val state = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmStateChange = remember(Unit) {
            fun(it: ModalBottomSheetValue): Boolean {
                val isHidden = it == ModalBottomSheetValue.Hidden
                if (!isDismissed && isHidden) {
                    navigationHandle.requestClose()
                    return isDismissed
                }
                return true
            }
        }
    )

    ModalBottomSheetLayout(
        sheetState = state,
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = .5.dp)
            ) {
                ExampleScreenTemplate(title = "Bottom Sheet")
            }
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = .5.dp)
            ) {}
        }
    )

    LaunchedEffect(isDismissed) {
        if (isDismissed) {
            state.hide()
            navigationHandle.onParentContainer {
                setBackstack { it.filterNot { it.navigationKey == navigationHandle.key } }
            }
        } else {
            state.show()
        }
    }
}