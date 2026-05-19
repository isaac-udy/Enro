package dev.enro.recipes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dev.enro.annotations.NavigationComponent
import dev.enro.controller.NavigationComponentConfiguration
import dev.enro.controller.createNavigationModule
import dev.enro.recipes.plugins.OpenedTimestampPlugin
import dev.enro.ui.contains
import dev.enro.ui.decorators.navigationDestinationDecorator
import dev.enro.ui.scenes.DialogSceneStrategy
import dev.enro.ui.scenes.isDirectOverlay

@NavigationComponent
object RecipesComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        plugin(OpenedTimestampPlugin())

        // Wrap every regular destination in a Material-themed surface so that
        // transitions (slide, fade, shared elements) animate over an opaque
        // background instead of showing through to whatever is composed
        // underneath. Dialogs and direct overlays are skipped — they're
        // intentionally rendered on top of the underlying scene.
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
