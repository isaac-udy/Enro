package nav.enro.core.controller.container

import nav.enro.core.NavigationHandle
import nav.enro.core.controller.NavigationController
import nav.enro.core.plugins.EnroPlugin
import nav.enro.core.result.EnroResult

internal class PluginContainer(
    plugins: List<EnroPlugin> = listOf()
) {
    private val plugins: List<EnroPlugin> = plugins + listOf(EnroResult())

    fun hasPlugin(block: (EnroPlugin) -> Boolean): Boolean {
        return plugins.any(block)
    }

    internal fun onAttached(navigationController: NavigationController) {
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