package dev.enro.context

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import dev.enro.platform.EnroLog

@Stable
internal class RootContextRegistry() {
    private val contexts = mutableStateListOf<RootContext>()

    fun register(context: RootContext) {
        val hadExistingContexts = contexts.removeAll { it.id == context.id}
        if (hadExistingContexts) {
            EnroLog.warn("Registered a RootContext that is already registered: ${context.id}")
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
