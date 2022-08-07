package dev.enro.core.compose.dialog

import android.annotation.SuppressLint
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.enro.core.NavigationAnimation
import dev.enro.core.compose.EnroContainer
import dev.enro.core.compose.container.ComposableNavigationContainer

@Deprecated("Use 'configureWindow' and set the soft input mode on the window directly")
enum class WindowInputMode(internal val mode: Int) {
    NOTHING(mode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING),
    PAN(mode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN),
    @Deprecated("See WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE")
    RESIZE(mode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE),
}

open class DialogConfiguration {
    internal var isDismissed = mutableStateOf(false)

    internal var animations: NavigationAnimation = NavigationAnimation.Resource(
        enter = 0,
        exit = 0
    )

    internal var softInputMode = WindowInputMode.RESIZE
    internal var configureWindow = mutableStateOf<(window: Window) -> Unit>({})

    class Builder internal constructor(
        private val dialogConfiguration: DialogConfiguration
    ) {
        fun setAnimations(animations: NavigationAnimation) {
            dialogConfiguration.animations = animations
        }

        @Deprecated("Use 'configureWindow' and set the soft input mode on the window directly")
        fun setWindowInputMode(mode: WindowInputMode) {
            dialogConfiguration.softInputMode = mode
        }

        fun configureWindow(block: (window: Window) -> Unit) {
            dialogConfiguration.configureWindow.value = block
        }
    }
}

@Composable
internal fun DialogConfiguration.ConfigureWindow() {
    val windowProvider = rememberDialogWindowProvider()
    DisposableEffect(
        windowProvider,
        configureWindow.value,
        softInputMode
    ) {
        val window = windowProvider?.window ?: return@DisposableEffect onDispose {  }

        window.setSoftInputMode(softInputMode.mode)
        configureWindow.value.invoke(window)

        onDispose { }
    }
}

interface DialogDestination {
    val dialogConfiguration: DialogConfiguration
}

val DialogDestination.isDismissed: Boolean
    @Composable get() = dialogConfiguration.isDismissed.value

@SuppressLint("ComposableNaming")
@Composable
fun DialogDestination.configureDialog(block: DialogConfiguration.Builder.() -> Unit) {
    remember {
        DialogConfiguration.Builder(dialogConfiguration)
            .apply(block)
    }
}

@Composable
internal fun EnroDialogContainer(
    controller: ComposableNavigationContainer,
    destination: DialogDestination
) {
    EnroContainer(container = controller)
    destination.dialogConfiguration.ConfigureWindow()
}