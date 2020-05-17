package nav.enro.example

import android.app.AlertDialog
import kotlinx.android.parcel.Parcelize
import nav.enro.annotations.NavigationDestination
import nav.enro.core.NavigationInstruction
import nav.enro.core.NavigationKey
import nav.enro.core.context.NavigationContext
import nav.enro.core.context.activity
import nav.enro.core.getNavigationHandle
import nav.enro.core.navigator.SyntheticDestination

@Parcelize
data class SimpleMessage(
    val title: String,
    val message: String,
    val positiveActionInstruction: NavigationInstruction.Open<*>? = null
) : NavigationKey

@NavigationDestination(SimpleMessage::class)
class SimpleMessageDestination : SyntheticDestination<SimpleMessage> {
    override fun process(
        navigationContext: NavigationContext<out Any, out NavigationKey>,
        instruction: NavigationInstruction.Open<SimpleMessage>
    ) {
        val activity = navigationContext.activity
        AlertDialog.Builder(activity).apply {
            setTitle(instruction.navigationKey.title)
            setMessage(instruction.navigationKey.message)
            setNegativeButton("Close") { _, _ -> }

            if(instruction.navigationKey.positiveActionInstruction != null) {
                setPositiveButton("Launch") {_, _ ->
                    navigationContext.activity
                        .getNavigationHandle<Nothing>()
                        .executeInstruction(instruction.navigationKey.positiveActionInstruction ?: return@setPositiveButton)
                }
            }

            show()
        }
    }
}