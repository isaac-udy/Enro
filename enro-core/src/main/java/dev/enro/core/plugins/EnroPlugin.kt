package dev.enro.core.plugins

import dev.enro.core.NavigationHandle
import dev.enro.core.controller.NavigationController
/**
 * Base class for creating plugins in Enro.
 *
 * Plugins can be used to extend Enro's functionality by providing lifecycle callbacks
 * and hooks into various parts of Enro's lifecycle.
 */
public abstract class EnroPlugin {

    /**
     * Called when this plugin is attached to a [NavigationController].
     *
     * This method is invoked when the plugin is registered with a NavigationController.
     *
     * @param navigationController The [NavigationController] instance to which this plugin is attached.
     */
    public open fun onAttached(navigationController: NavigationController) {}

    /**
     * Called when this plugin is detached from a [NavigationController].
     *
     * This method is invoked when the plugin is unregistered from a NavigationController, or
     * the NavigationController is uninstalled from the Application (which may happen during tests).
     *
     * @param navigationController The [NavigationController] instance from which this plugin is detached.
     */
    public open fun onDetached(navigationController: NavigationController) {}

    /**
     * This method is invoked when a navigation handle representing a screen is opened.
     *
     * @param navigationHandle The [NavigationHandle] associated with the opened screen.
     */
    public open fun onOpened(navigationHandle: NavigationHandle) {}

    /**
     * This method is invoked when a NavigationHandle representing a screen becomes the active screen.
     *
     * @param navigationHandle The [NavigationHandle] associated with the active screen.
     */
    public open fun onActive(navigationHandle: NavigationHandle) {}

    /**
     * This method is invoked when a NavigationHandle representing a screen is closed.
     *
     * @param navigationHandle The [NavigationHandle] associated with the closed screen.
     */
    public open fun onClosed(navigationHandle: NavigationHandle) {}
}