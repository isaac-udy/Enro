package dev.enro.context

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import dev.enro.controller.repository.PluginRepository
import dev.enro.platform.EnroLog

@Stable
internal class RootContextRegistry(
    private val pluginRepository: PluginRepository
) {
    private val contexts = mutableStateListOf<RootContext>()

    fun register(context: RootContext) {
        val hasContext = contexts.any { it.id == context.id}
        if (hasContext) {
            EnroLog.warn("Attempted to register a RootContext that is already registered: ${context.id}")
            return
        }
        contexts.add(context)
        pluginRepository.onRootContextAttached(context)
    }

    fun unregister(context: RootContext) {
        contexts.remove(context)
        pluginRepository.onRootContextDetached(context)
    }

    fun getAllContexts(): List<RootContext> {
        return contexts
    }
}
