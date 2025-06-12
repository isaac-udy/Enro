package dev.enro.plugin

import dev.enro.EnroController
import dev.enro.NavigationHandle
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.ui.NavigationDestination

public abstract class NavigationPlugin {
    public open fun onAttached(controller: EnroController) {}
    public open fun onDetached(controller: EnroController) {}

    public open fun onOpened(navigationHandle: NavigationHandle<*>) {}
    public open fun onActive(navigationHandle: NavigationHandle<*>) {}
    public open fun onClosed(navigationHandle: NavigationHandle<*>) {}
    
    /**
     * Called when a navigation destination is created, allowing plugins to modify its metadata
     * before rendering.
     *
     * Plugins can use this to alter how destinations are rendered by adding or overriding metadata.
     * For example, a compatibility plugin might change a destination to render as an overlay by
     * setting the "directOverlay" metadata.
     *
     * Setting a value in the additionalMetadata map to null will remove it from the destination's
     * metadata.
     *
     * **Warning:** This is an advanced API. Modifying metadata incorrectly can break the way that
     * destinations are rendered.
     *
     * @param destination The newly created navigation destination
     * @param additionalMetadata A mutable map for adding/modifying metadata. Values here override
     *                          existing destination metadata with the same key.
     */
    @AdvancedEnroApi
    public open fun onDestinationCreated(
        destination: NavigationDestination<*>,
        additionalMetadata: MutableMap<String, Any?>,
    ) {}
}