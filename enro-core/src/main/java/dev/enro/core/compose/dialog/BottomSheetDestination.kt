package dev.enro.core.compose.dialog

import android.annotation.SuppressLint
import android.view.Window
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.core.DefaultAnimations
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.getNavigationHandle
import dev.enro.core.requestClose

@ExperimentalMaterialApi
public class BottomSheetConfiguration : DialogConfiguration() {
    internal var skipHalfExpanded: Boolean = false
    internal lateinit var bottomSheetState: ModalBottomSheetState

    init {
        animations = DefaultAnimations.none
    }

    public class Builder internal constructor(
        private val bottomSheetConfiguration: BottomSheetConfiguration
    ) {
        public fun setSkipHalfExpanded(skipHalfExpanded: Boolean) {
            bottomSheetConfiguration.skipHalfExpanded = skipHalfExpanded
        }

        @Deprecated("Use 'configureWindow' and set the soft input mode on the window directly")
        public fun setWindowInputMode(mode: WindowInputMode) {
            bottomSheetConfiguration.softInputMode = mode
        }

        public fun configureWindow(block: (window: Window) -> Unit) {
            bottomSheetConfiguration.configureWindow.value = block
        }
    }
}

@ExperimentalMaterialApi
public interface BottomSheetDestination {
    public val bottomSheetConfiguration: BottomSheetConfiguration
}

@ExperimentalMaterialApi
public val BottomSheetDestination.bottomSheetState: ModalBottomSheetState get() = bottomSheetConfiguration.bottomSheetState

@ExperimentalMaterialApi
@SuppressLint("ComposableNaming")
@Composable
public fun BottomSheetDestination.configureBottomSheet(block: BottomSheetConfiguration.Builder.() -> Unit) {
    remember {
        BottomSheetConfiguration.Builder(bottomSheetConfiguration)
            .apply(block)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun EnroBottomSheetContainer(
    composableDestination: ComposableDestination,
    destination: BottomSheetDestination
) {
    val state = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmStateChange = remember(Unit) {
            fun(it: ModalBottomSheetValue): Boolean {
                val isHidden = it == ModalBottomSheetValue.Hidden
                val isHalfExpandedAndSkipped = it == ModalBottomSheetValue.HalfExpanded
                        && destination.bottomSheetConfiguration.skipHalfExpanded
                val isDismissed = destination.bottomSheetConfiguration.isDismissed.value

                if (!isDismissed && (isHidden || isHalfExpandedAndSkipped)) {
                    composableDestination.context.getNavigationHandle().requestClose()
                    return destination.bottomSheetConfiguration.isDismissed.value
                }
                return true
            }
        }
    )
    destination.bottomSheetConfiguration.bottomSheetState = state
    ModalBottomSheetLayout(
        sheetState = state,
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = .5.dp)
            ) {
                composableDestination.Render()
            }
            destination.bottomSheetConfiguration.ConfigureWindow()
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = .5.dp)
            ) {}
        }
    )

    LaunchedEffect(destination.bottomSheetConfiguration.isDismissed.value) {
        if (destination.bottomSheetConfiguration.isDismissed.value) {
            state.hide()
        }
        else {
            state.show()
        }
    }
}