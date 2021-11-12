package dev.enro.core.controller.container

import dev.enro.core.NavigationHandle
import dev.enro.core.controller.NavigationController
import dev.enro.core.plugins.EnroPlugin
import dev.enro.core.result.EnroResult

internal class PluginContainer {
    private val plugins: MutableList<EnroPlugin> = mutableListOf()
    private var attachedController: NavigationController? = null

    fun addPlugins(
        plugins: List<EnroPlugin>
    ) {
        this.plugins += plugins
        attachedController?.let { attachedController ->
            plugins.forEach { it.onAttached(attachedController) }
        }
    }

    fun hasPlugin(block: (EnroPlugin) -> Boolean): Boolean {
        return plugins.any(block)
    }

    internal fun onAttached(navigationController: NavigationController) {
        require(attachedController == null) {
            "This PluginContainer is already attached to a NavigationController!"
        }
        attachedController = navigationController
        plugins.forEach { it.onAttached(navigationController) }
    }

    internal fun onOpened(navigationHandle: NavigationHandle) {
        plugins.forEach { it.onOpened(navigationHandle) }
    }

    internal fun onActive(navigationHandle: NavigationHandle) {
        plugins.forEach { it.onActive(navigationHandle) }
    }

    internal fun onClosed(navigationHandle: NavigationHandle) {
        plugins.forEach { it.onClosed(navigationHandle) }
    }
}