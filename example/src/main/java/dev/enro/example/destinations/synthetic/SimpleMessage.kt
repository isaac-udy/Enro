package dev.enro.example.destinations.synthetic

import android.app.AlertDialog
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.destination.synthetic.syntheticDestination
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class SimpleMessage(
    val title: String,
    val message: String,
    val positiveActionInstruction: @RawValue NavigationInstruction? = null
) : NavigationKey.SupportsPresent

@NavigationDestination(SimpleMessage::class)
val simpleMessageDestination = syntheticDestination<SimpleMessage> {
    val activity = navigationContext.activity
    AlertDialog.Builder(activity).apply {
        setTitle(key.title)
        setMessage(key.message)
        setNegativeButton("Close") { _, _ -> }

        if(key.positiveActionInstruction != null) {
            setPositiveButton("Launch") {_, _ ->
                navigationContext
                    .getNavigationHandle()
                    .executeInstruction(key.positiveActionInstruction!!)
            }
        }

        show()
    }
}
