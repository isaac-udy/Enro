package dev.enro.recipes

import dev.enro.annotations.NavigationComponent
import dev.enro.controller.NavigationComponentConfiguration
import dev.enro.controller.createNavigationModule

@NavigationComponent
object RecipesComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
    }
)
