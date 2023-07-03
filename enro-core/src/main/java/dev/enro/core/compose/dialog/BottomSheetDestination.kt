package dev.enro.core.compose.dialog

import android.annotation.SuppressLint
import android.view.Window
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.enro.core.AnimationPair
import dev.enro.core.DefaultAnimations
import dev.enro.core.compose.EnroContainer
import dev.enro.core.compose.EnroContainerController
import dev.enro.core.getNavigationHandle
import dev.enro.core.requestClose

@ExperimentalMaterialApi
class BottomSheetConfiguration : DialogConfiguration() {
    internal var animatesToInitialState: Boolean = true
    internal var animatesToHiddenOnClose: Boolean = true
    internal var skipHalfExpanded: Boolean = false
    internal lateinit var bottomSheetState: ModalBottomSheetState

    init {
        animations = DefaultAnimations.none
    }

    class Builder internal constructor(
        private val bottomSheetConfiguration: BottomSheetConfiguration
    ) {
        fun setAnimatesToInitialState(animatesToInitialState: Boolean) {
            bottomSheetConfiguration.animatesToInitialState = animatesToInitialState
        }

        fun setAnimatesToHiddenOnClose(animatesToHidden: Boolean) {
            bottomSheetConfiguration.animatesToHiddenOnClose = animatesToHidden
        }

        fun setSkipHalfExpanded(skipHalfExpanded: Boolean) {
            bottomSheetConfiguration.skipHalfExpanded = skipHalfExpanded
        }

        fun setScrimColor(color: Color) {
            bottomSheetConfiguration.scrimColor = color
        }

        fun setAnimations(animations: AnimationPair) {
            bottomSheetConfiguration.animations = animations
        }

        @Deprecated("Use 'configureWindow' and set the soft input mode on the window directly")
        fun setWindowInputMode(mode: WindowInputMode) {
            bottomSheetConfiguration.softInputMode = mode
        }

        fun configureWindow(block: (window: Window) -> Unit) {
            bottomSheetConfiguration.configureWindow.value = block
        }
    }
}

@ExperimentalMaterialApi
interface BottomSheetDestination {
    val bottomSheetConfiguration: BottomSheetConfiguration
}

@ExperimentalMaterialApi
val BottomSheetDestination.bottomSheetState get() = bottomSheetConfiguration.bottomSheetState

@ExperimentalMaterialApi
@SuppressLint("ComposableNaming")
@Composable
fun BottomSheetDestination.configureBottomSheet(block: BottomSheetConfiguration.Builder.() -> Unit) {
    remember {
        BottomSheetConfiguration.Builder(bottomSheetConfiguration)
            .apply(block)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun EnroBottomSheetContainer(
    controller: EnroContainerController,
    destination: BottomSheetDestination
) {
    val state = rememberModalBottomSheetState(
        initialValue = if(destination.bottomSheetConfiguration.skipHalfExpanded) ModalBottomSheetValue.Expanded else ModalBottomSheetValue.HalfExpanded,
        confirmValueChange = remember(Unit) {
            fun(it: ModalBottomSheetValue): Boolean {
                val isHidden = it == ModalBottomSheetValue.Hidden
                val isHalfExpandedAndSkipped = it == ModalBottomSheetValue.HalfExpanded
                        && destination.bottomSheetConfiguration.skipHalfExpanded
                val isDismissed = destination.bottomSheetConfiguration.isDismissed.value

                if (!isDismissed && (isHidden || isHalfExpandedAndSkipped)) {
                    controller.activeContext?.getNavigationHandle()?.requestClose()
                    return destination.bottomSheetConfiguration.isDismissed.value
                }
                return true
            }
        }, skipHalfExpanded = destination.bottomSheetConfiguration.skipHalfExpanded
    )
    destination.bottomSheetConfiguration.bottomSheetState = state
    LaunchedEffect(destination.bottomSheetConfiguration.isDismissed.value) {
        if(destination.bottomSheetConfiguration.isDismissed.value && destination.bottomSheetConfiguration.animatesToHiddenOnClose) {
            state.hide()
        }
    }

    ModalBottomSheetLayout(
        sheetState = state,
        sheetContent = {
            EnroContainer(
                controller = controller,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 0.5.dp)
            )
        },
        content = {}
    )
}