package dev.enro.recipes

import dev.enro.annotations.NavigationComponent
import dev.enro.controller.NavigationComponentConfiguration
import dev.enro.controller.createNavigationModule
import dev.enro.recipes.plugins.OpenedTimestampPlugin

@NavigationComponent
object RecipesComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        plugin(OpenedTimestampPlugin())
    }
)
