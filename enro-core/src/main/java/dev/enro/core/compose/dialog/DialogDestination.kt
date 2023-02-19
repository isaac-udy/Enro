package dev.enro.core.compose.dialog

import android.annotation.SuppressLint
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.enro.core.*

@Deprecated("Use 'configureWindow' and set the soft input mode on the window directly")
public enum class WindowInputMode(internal val mode: Int) {
    NOTHING(mode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING),
    PAN(mode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN),

    @Deprecated("See WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE")
    RESIZE(mode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE),
}

public open class DialogConfiguration {
    internal var isDismissed = mutableStateOf(false)

    internal var animations: NavigationAnimation = NavigationAnimation.Resource(
        enter = 0,
        exit = 0
    )

    internal var softInputMode: WindowInputMode? = null
    internal var configureWindow = mutableStateOf<(window: Window) -> Unit>({})

    public class Builder internal constructor(
        private val dialogConfiguration: DialogConfiguration
    ) {
        public fun setAnimations(animations: NavigationAnimation) {
            dialogConfiguration.animations = animations
        }

        @Deprecated("Use 'configureWindow' and set the soft input mode on the window directly")
        public fun setWindowInputMode(mode: WindowInputMode) {
            dialogConfiguration.softInputMode = mode
        }

        public fun configureWindow(block: (window: Window) -> Unit) {
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

        softInputMode?.mode?.let {
            window.setSoftInputMode(it)
        }
        configureWindow.value.invoke(window)

        onDispose { }
    }
}

public interface DialogDestination {
    public val dialogConfiguration: DialogConfiguration
}

public val DialogDestination.isDismissed: Boolean
    get() = dialogConfiguration.isDismissed.value

@SuppressLint("ComposableNaming")
@Composable
public fun DialogDestination.configureDialog(block: DialogConfiguration.Builder.() -> Unit) {
    remember {
        DialogConfiguration.Builder(dialogConfiguration)
            .apply(block)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun EnroDialogContainer(
    navigationHandle: NavigationHandle,
    destination: DialogDestination,
    content: @Composable () -> Unit
) {
    if (destination.isDismissed) return
    Dialog(
        onDismissRequest = { navigationHandle.requestClose() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        content()
    }
    destination.dialogConfiguration.ConfigureWindow()
}