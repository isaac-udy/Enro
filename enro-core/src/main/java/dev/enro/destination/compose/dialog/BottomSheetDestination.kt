@file:Suppress("DEPRECATION", "DeprecatedCallableAddReplaceWith")

package dev.enro.destination.compose.dialog

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
import dev.enro.animation.DefaultAnimations
import dev.enro.core.*
import dev.enro.core.container.setBackstack

@ExperimentalMaterialApi
@Deprecated("See the BottomSheetDestination interface")
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

/**
 * Instead of creating destinations like this:
 * ```
 * @Composable
 * fun BottomSheetDestination.ExampleDestination() {
 *     configureBottomSheet()
 * }
 * ```
 *
 * please use the `BottomSheetDestination` function, like this:
 * @Composable
 * fun ExampleDestination() = BottomSheetDestination(/* configuration */) { state ->
 *     configureBottomSheet()
 * }
 */
@ExperimentalMaterialApi
@Deprecated("Don't create destinations that use BottomSheetDestination as a receiver type, instead use the BottomSheetDestination function inside of the destination")
public interface BottomSheetDestination {
    public val bottomSheetConfiguration: BottomSheetConfiguration
}

@OptIn(ExperimentalMaterialApi::class)
@Deprecated("See the BottomSheetDestination interface")
public val BottomSheetDestination.isDismissed: Boolean
    get() = bottomSheetConfiguration.isDismissed.value

@ExperimentalMaterialApi
@Deprecated("See the BottomSheetDestination interface")
public val BottomSheetDestination.bottomSheetState: ModalBottomSheetState get() = bottomSheetConfiguration.bottomSheetState

@ExperimentalMaterialApi
@SuppressLint("ComposableNaming")
@Composable
@Deprecated("See the BottomSheetDestination interface")
public fun BottomSheetDestination.configureBottomSheet(block: BottomSheetConfiguration.Builder.() -> Unit) {
    remember {
        BottomSheetConfiguration.Builder(bottomSheetConfiguration)
            .apply(block)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun EnroBottomSheetContainer(
    navigationHandle: NavigationHandle,
    destination: BottomSheetDestination,
    content: @Composable () -> Unit
) {
    val state = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmValueChange = remember(Unit) {
            fun(it: ModalBottomSheetValue): Boolean {
                val isHidden = it == ModalBottomSheetValue.Hidden
                val isHalfExpandedAndSkipped = it == ModalBottomSheetValue.HalfExpanded
                        && destination.bottomSheetConfiguration.skipHalfExpanded
                val isDismissed = destination.bottomSheetConfiguration.isDismissed.value

                if (!isDismissed && (isHidden || isHalfExpandedAndSkipped)) {
                    navigationHandle.requestClose()
                    return destination.bottomSheetConfiguration.isDismissed.value
                }
                return true
            }
        },
        skipHalfExpanded = destination.bottomSheetConfiguration.skipHalfExpanded,
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
                content()
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
            navigationHandle.onParentContainer {
                setBackstack { it.filterNot { it.navigationKey == navigationHandle.key } }
            }
        }
    }
    LaunchedEffect(state) {
        state.show()
    }
}