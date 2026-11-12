package dev.enro.recipes

import dev.enro.recipes.windows.MultipleWindowsRecipe

internal actual val platformRecipes: List<RecipeEntry> = listOf(
    RecipeEntry(
        key = MultipleWindowsRecipe,
        title = "Multiple Windows",
        description = "A destination that opens in its own window and returns a result " +
            "when it closes.",
    ),
)
