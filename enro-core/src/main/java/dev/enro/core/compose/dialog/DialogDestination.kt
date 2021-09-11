package dev.enro.core.compose.dialog

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import dev.enro.core.AnimationPair
import dev.enro.core.compose.EnroContainer
import dev.enro.core.compose.EnroContainerController


open class DialogConfiguration {
    internal var scrimColor: Color = Color(0x52000000)
    internal var animations: AnimationPair = AnimationPair.Attr(
        enter = android.R.attr.activityOpenEnterAnimation,
        exit = android.R.attr.activityCloseExitAnimation
    )

    class Builder internal constructor(
        private val dialogConfiguration: DialogConfiguration
    ) {
        fun setScrimColor(color: Color) {
            dialogConfiguration.scrimColor = color
        }

        fun setAnimations(animations: AnimationPair) {
            dialogConfiguration.animations = animations
        }
    }
}

interface DialogDestination {
    val dialogConfiguration: DialogConfiguration
}

@SuppressLint("ComposableNaming")
@Composable
fun DialogDestination.configureDialog(block: DialogConfiguration.Builder.() -> Unit) {
    rememberSaveable(true) {
        val builder = DialogConfiguration.Builder(dialogConfiguration)
        block(builder)
        true
    }
}

@Composable
internal fun EnroDialogContainer(
    controller: EnroContainerController,
    destination: DialogDestination
) {
    EnroContainer(controller = controller)
}