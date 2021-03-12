package dev.enro.example

import android.app.AlertDialog
import kotlinx.android.parcel.Parcelize
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.NavigationContext
import dev.enro.core.activity
import dev.enro.core.getNavigationHandle
import dev.enro.core.synthetic.SyntheticDestination

@Parcelize
data class SimpleMessage(
    val title: String,
    val message: String,
    val positiveActionInstruction: NavigationInstruction.Open? = null
) : NavigationKey

@NavigationDestination(SimpleMessage::class)
class SimpleMessageDestination : SyntheticDestination<SimpleMessage> {
    override fun process(
        navigationContext: NavigationContext<out Any>,
        key: SimpleMessage,
        instruction: NavigationInstruction.Open
    ) {
        val activity = navigationContext.activity
        AlertDialog.Builder(activity).apply {
            setTitle(key.title)
            setMessage(key.message)
            setNegativeButton("Close") { _, _ -> }

            if(key.positiveActionInstruction != null) {
                setPositiveButton("Launch") {_, _ ->
                    navigationContext.activity
                        .getNavigationHandle()
                        .executeInstruction(key.positiveActionInstruction)
                }
            }

            show()
        }
    }
}