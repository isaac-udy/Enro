package dev.enro.recipes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dev.enro.annotations.NavigationComponent
import dev.enro.controller.NavigationComponentConfiguration
import dev.enro.controller.createNavigationModule
import dev.enro.recipes.deeplink.deepLinkModule
import dev.enro.recipes.interceptor.auditInterceptor
import dev.enro.recipes.plugins.OpenedTimestampPlugin
import dev.enro.ui.contains
import dev.enro.ui.decorators.navigationDestinationDecorator
import dev.enro.ui.scenes.DialogSceneStrategy
import dev.enro.ui.scenes.isDirectOverlay

@NavigationComponent
object RecipesComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        plugin(OpenedTimestampPlugin())

        // deepLinkModule registers the path bindings of the Basic Deep Link recipe.
        module(deepLinkModule)

        // An interceptor registered here receives operations from every
        // container; see the Module Interceptor recipe.
        interceptor(auditInterceptor)

        // The decorator wraps each destination in an opaque Surface, so a
        // transition does not show the destination below. Dialogs and direct
        // overlays are not wrapped, because the scene below must stay visible.
        decorator {
            navigationDestinationDecorator { destination ->
                val isOverlay = destination.isDirectOverlay() ||
                    destination.metadata.contains(DialogSceneStrategy.DialogPropertiesKey)
                if (isOverlay) {
                    destination.Content()
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        destination.Content()
                    }
                }
            }
        }
    }
)
