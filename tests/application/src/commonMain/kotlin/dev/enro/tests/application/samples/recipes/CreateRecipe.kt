package dev.enro.tests.application.samples.recipes

import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.tests.application.samples.recipes.create.AddIngredients
import dev.enro.tests.application.samples.recipes.create.AddSteps
import dev.enro.tests.application.samples.recipes.create.EnterCookingTime
import dev.enro.tests.application.samples.recipes.create.EnterDescription
import dev.enro.tests.application.samples.recipes.create.EnterServings
import dev.enro.tests.application.samples.recipes.create.EnterTitle
import dev.enro.ui.destinations.managedFlowDestination
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
object CreateRecipe : NavigationKey.WithResult<Recipe>

@NavigationDestination(CreateRecipe::class)
val createRecipeDestination = managedFlowDestination<CreateRecipe, Recipe>(
    flow = {
        val title = open { EnterTitle }
        val description = open { EnterDescription }
        val cookingTimeMinutes = open { EnterCookingTime }
        val servings = open { EnterServings }
        val ingredients = open { AddIngredients }
        val instructions = open { AddSteps }
        val notes = open { EnterDescription }

        Recipe(
            id = Uuid.random().toString(),
            title = title.title,
            emoji = title.emoji,
            description = description,
            ingredients = ingredients,
            instructions = instructions,
            cookingTimeMinutes = cookingTimeMinutes,
            servings = servings,
            notes = notes
        )
    }
)