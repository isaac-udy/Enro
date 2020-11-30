package nav.enro.example

import android.app.AlertDialog
import kotlinx.android.parcel.Parcelize
import nav.enro.annotations.NavigationDestination
import nav.enro.core.NavigationInstruction
import nav.enro.core.NavigationKey
import nav.enro.core.NavigationContext
import nav.enro.core.activity
import nav.enro.core.getNavigationHandle
import nav.enro.core.synthetic.SyntheticDestination

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
        instruction: NavigationInstruction.Open
    ) {
        val key = instruction.navigationKey as SimpleMessage
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