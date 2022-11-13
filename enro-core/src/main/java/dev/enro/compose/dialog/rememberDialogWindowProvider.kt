package dev.enro.compose.dialog

import android.view.View
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.findFragment

@Composable
internal fun rememberDialogWindowProvider(): DialogWindowProvider? {
    val localView = LocalView.current
    return remember(localView) {
        var view: View? = localView
        while (view != null) {
            if (view is DialogWindowProvider) return@remember view
            view = view.parent as? View
        }

        val fragment = runCatching { localView.findFragment<DialogFragment>() }
            .getOrNull()

        return@remember if (fragment != null) {
            object: DialogWindowProvider {
                override val window: Window
                    get() = fragment.requireDialog().window!!
            }
        } else null
    }
}
