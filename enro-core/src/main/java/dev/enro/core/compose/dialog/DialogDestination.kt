package dev.enro.core.compose.dialog

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import dev.enro.core.AnimationPair
import dev.enro.core.ComposableNavigationContainer
import dev.enro.core.compose.EnroContainer


open class DialogConfiguration {
    internal var isDismissed = mutableStateOf(false)

    internal var scrimColor: Color = Color.Transparent
    internal var animations: AnimationPair = AnimationPair.Resource(
        enter = 0,
        exit = 0
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

val DialogDestination.isDismissed: Boolean
    @Composable get() = dialogConfiguration.isDismissed.value

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
    controller: ComposableNavigationContainer,
    destination: DialogDestination
) {
    EnroContainer(controller = controller)
}