package dev.enro.context

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import dev.enro.platform.EnroLog

@Stable
internal class RootContextRegistry() {
    private val contexts = mutableStateListOf<RootContext>()

    // A platform host and RootNavigationHandle.bindContext can both register the same context.
    fun register(context: RootContext) {
        if (contexts.any { it === context }) return
        val hadExistingContexts = contexts.removeAll { it.id == context.id }
        if (hadExistingContexts) {
            EnroLog.warn("Registered a RootContext with the same id as a registered RootContext: ${context.id}")
        }
        contexts.add(context)
    }

    fun unregister(context: RootContext) {
        contexts.remove(context)
    }

    fun getAllContexts(): List<RootContext> {
        return contexts
    }
}
