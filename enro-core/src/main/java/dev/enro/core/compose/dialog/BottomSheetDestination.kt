package dev.enro.core.compose.dialog

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
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

        fun setWindowInputMode(mode: WindowInputMode) {
            bottomSheetConfiguration.softInputMode.value = mode
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
            if(it == ModalBottomSheetValue.HalfExpanded && destination.bottomSheetConfiguration.skipHalfExpanded) {
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
        if(destination.bottomSheetConfiguration.isDismissed.value && destination.bottomSheetConfiguration.animatesToHiddenOnClose) {
            state.hide()
        }
    }

    ModalBottomSheetLayout(
        sheetState = state,
        sheetContent = {
            EnroContainer(
                controller = controller,
                modifier = Modifier.fillMaxWidth()
            )
        },
        content = {}
    )

    LaunchedEffect(true) {
        if(destination.bottomSheetConfiguration.animatesToInitialState) {
            state.show()
        } else {
            state.snapTo(ModalBottomSheetValue.Expanded)
        }
    }
}