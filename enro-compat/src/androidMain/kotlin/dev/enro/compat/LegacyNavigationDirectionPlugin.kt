package dev.enro.compat

import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.NavigationDirection
import dev.enro.plugin.NavigationPlugin
import dev.enro.ui.NavigationDestination
import dev.enro.ui.scenes.DirectOverlaySceneStrategy

internal object LegacyNavigationDirectionPlugin : NavigationPlugin() {
    @AdvancedEnroApi
    override fun onDestinationCreated(
        destination: NavigationDestination<*>,
        additionalMetadata: MutableMap<String, Any?>,
    ) {
        val direction = destination.instance.metadata.get(NavigationDirection.MetadataKey)
        if (direction == null) return
        if (direction != NavigationDirection.Present) return
        additionalMetadata += DirectOverlaySceneStrategy.overlay()
    }
}