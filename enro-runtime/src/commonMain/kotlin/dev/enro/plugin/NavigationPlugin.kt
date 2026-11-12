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
     * Called when a [NavigationDestination] is created, before the destination is rendered.
     *
     * An entry added to [additionalMetadata] replaces the entry with the same key in the
     * metadata of [destination]. A null value removes the key from the metadata of
     * [destination]. Each plugin receives the same [additionalMetadata] map, in plugin order.
     */
    @AdvancedEnroApi
    public open fun onDestinationCreated(
        destination: NavigationDestination<*>,
        additionalMetadata: MutableMap<String, Any?>,
    ) {}
}