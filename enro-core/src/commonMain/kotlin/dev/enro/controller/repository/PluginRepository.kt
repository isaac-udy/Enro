package dev.enro.controller.repository

import dev.enro.EnroController
import dev.enro.NavigationContainer
import dev.enro.NavigationHandle
import dev.enro.NavigationTransition
import dev.enro.plugin.NavigationPlugin

internal class PluginRepository {
    private val plugins: MutableList<NavigationPlugin> = mutableListOf()
    private var attachedController: EnroController? = null

    fun addPlugins(
        plugins: List<NavigationPlugin>
    ) {
        if (plugins.isEmpty()) return
        this.plugins += plugins
        attachedController?.let { attachedController ->
            plugins.forEach { it.onAttached(attachedController) }
        }
    }

    fun removePlugins(
        plugins: List<NavigationPlugin>,
    ) {
        this.plugins -= plugins
        attachedController?.let { attachedController ->
            plugins.forEach { it.onDetached(attachedController) }
        }
    }

    fun onAttached(controller: EnroController) {
        require(attachedController == null) {
            "This PluginContainer is already attached to a NavigationController!"
        }
        attachedController = controller
        plugins.forEach { it.onAttached(controller) }
    }

    fun onDetached(controller: EnroController) {
        if (attachedController == null) return
        plugins.forEach { it.onDetached(controller) }
        attachedController = null
    }

    fun onTransitionApplied(container: NavigationContainer, transition: NavigationTransition) {
        plugins.forEach { it.onTransitionApplied(container, transition) }
    }

    fun onOpened(navigationHandle: NavigationHandle<*>) {
        plugins.forEach { it.onOpened(navigationHandle) }
    }

    fun onActive(navigationHandle: NavigationHandle<*>) {
        plugins.forEach { it.onActive(navigationHandle) }
    }

    fun onClosed(navigationHandle: NavigationHandle<*>) {
        plugins.forEach { it.onClosed(navigationHandle) }
    }
}