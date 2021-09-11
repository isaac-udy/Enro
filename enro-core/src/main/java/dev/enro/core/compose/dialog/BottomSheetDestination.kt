package dev.enro.core.compose.dialog

import android.annotation.SuppressLint
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import dev.enro.core.AnimationPair
import dev.enro.core.compose.EnroContainer
import dev.enro.core.compose.EnroContainerController
import dev.enro.core.getNavigationHandle
import dev.enro.core.requestClose

@OptIn(ExperimentalMaterialApi::class)
class BottomSheetConfiguration : DialogConfiguration() {
    internal var initialState: ModalBottomSheetValue = ModalBottomSheetValue.HalfExpanded
    internal var snapToInitialState: Boolean = false
    internal lateinit var bottomSheetState: ModalBottomSheetState

    class Builder internal constructor(
        private val bottomSheetConfiguration: BottomSheetConfiguration
    ) {
        fun initialStateIsHalfExpanded() {
            bottomSheetConfiguration.initialState = ModalBottomSheetValue.HalfExpanded
        }

        fun initialStateIsExpanded() {
            bottomSheetConfiguration.initialState = ModalBottomSheetValue.Expanded
        }

        fun snapToInitialState() {
            bottomSheetConfiguration.snapToInitialState = true
        }

        fun setScrimColor(color: Color) {
            bottomSheetConfiguration.scrimColor = color
        }

        fun setAnimations(animations: AnimationPair) {
            bottomSheetConfiguration.animations = animations
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
interface BottomSheetDestination {
    val bottomSheetConfiguration: BottomSheetConfiguration
}

val BottomSheetDestination.bottomSheetState get() = bottomSheetConfiguration.bottomSheetState

@SuppressLint("ComposableNaming")
@Composable
fun BottomSheetDestination.configureBottomSheet(block: BottomSheetConfiguration.Builder.() -> Unit) {
    rememberSaveable(true) {
        val configBuilder = BottomSheetConfiguration.Builder(bottomSheetConfiguration)
        block(configBuilder)
        true
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun EnroBottomSheetContainer(
    controller: EnroContainerController,
    destination: BottomSheetDestination
) {
    val state = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmStateChange = {
            if (it == ModalBottomSheetValue.Hidden) {
                val isDismissed = destination.bottomSheetConfiguration.isDismissed.value
                if(!isDismissed) {
                    controller.activeContext?.getNavigationHandle()?.requestClose()
                    return@rememberModalBottomSheetState destination.bottomSheetConfiguration.isDismissed.value
                }
            }
            true
        }
    )
    destination.bottomSheetConfiguration.bottomSheetState = state
    LaunchedEffect(destination.bottomSheetConfiguration.isDismissed.value) {
        if(destination.bottomSheetConfiguration.isDismissed.value) {
            state.hide()
        }
    }

    ModalBottomSheetLayout(
        sheetState = state,
        scrimColor = Color.Transparent,
        sheetContent = {
            EnroContainer(controller = controller)
        },
        content = {}
    )

    LaunchedEffect(true) {
        if(destination.bottomSheetConfiguration.snapToInitialState) {
            state.snapTo(destination.bottomSheetConfiguration.initialState)
        } else {
            state.animateTo(destination.bottomSheetConfiguration.initialState)
        }
    }
}