package dev.enro.recipes

import dev.enro.recipes.interop.FragmentAndActivityRecipe

internal actual val platformRecipes: List<RecipeEntry> = listOf(
    RecipeEntry(
        key = FragmentAndActivityRecipe,
        title = "Fragment and Activity",
        description = "A Fragment and an Activity as destinations, opened from and " +
            "opening Composable destinations. The Activity returns a result.",
    ),
)
