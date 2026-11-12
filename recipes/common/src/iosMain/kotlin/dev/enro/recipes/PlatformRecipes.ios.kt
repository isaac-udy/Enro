package dev.enro.recipes

import dev.enro.recipes.interop.UIViewControllerRecipe

internal actual val platformRecipes: List<RecipeEntry> = listOf(
    RecipeEntry(
        key = UIViewControllerRecipe,
        title = "UIViewController",
        description = "A UIViewController as a destination: embedded in the container, " +
            "presented modally, and pushed onto a UINavigationController.",
    ),
)
