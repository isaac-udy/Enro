package dev.enro.destination.flow

import androidx.lifecycle.ViewModel
import dev.enro.core.result.flows.registerForFlowResult

internal class ManagedFlowViewModel : ViewModel() {
    private val flow by registerForFlowResult<Any?>(
        isManuallyStarted = true,
        flow = { },
        onCompleted = { },
    )

    internal fun bind(
        destination: ManagedFlowDestination<*, *>,
    ) {
        @Suppress("UNCHECKED_CAST")
        destination as ManagedFlowDestination<*, Any?>

        flow.flow = {
            destination.run { flow() }
        }
        flow.onCompleted = {
            destination.onCompleted(it)
        }
        flow.update()
    }
}
