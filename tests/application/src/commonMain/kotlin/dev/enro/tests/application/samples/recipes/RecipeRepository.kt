package dev.enro.tests.application.samples.recipes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object RecipeRepository {
    private val _recipes = MutableStateFlow(
        listOf(
            Recipe(
                id = "1",
                title = "Spaghetti Carbonara",
                emoji = "🍝",
                description = "Classic Italian pasta dish with eggs, cheese, and pancetta",
                ingredients = listOf(
                    "400g spaghetti",
                    "200g pancetta or guanciale",
                    "4 large eggs",
                    "100g Parmesan cheese, grated",
                    "Salt and black pepper to taste"
                ),
                instructions = listOf(
                    "Cook spaghetti in salted boiling water until al dente",
                    "While pasta cooks, fry pancetta until crispy",
                    "Beat eggs with grated Parmesan and black pepper",
                    "Drain pasta, reserving some pasta water",
                    "Remove pan from heat, add pasta to pancetta",
                    "Quickly stir in egg mixture, adding pasta water to create creamy sauce",
                    "Serve immediately with extra Parmesan"
                ),
                cookingTimeMinutes = 20,
                servings = 4
            ),
            Recipe(
                id = "2",
                title = "Chicken Tikka Masala",
                emoji = "🍛",
                description = "Creamy and flavorful Indian curry dish",
                ingredients = listOf(
                    "500g chicken breast, cubed",
                    "200ml yogurt",
                    "2 tbsp tikka masala spice",
                    "400ml coconut milk",
                    "1 onion, diced",
                    "3 garlic cloves",
                    "1 tbsp ginger paste",
                    "2 tbsp tomato paste"
                ),
                instructions = listOf(
                    "Marinate chicken in yogurt and spices for 30 minutes",
                    "Cook marinated chicken until browned",
                    "Sauté onion, garlic, and ginger",
                    "Add tomato paste and cook for 2 minutes",
                    "Add coconut milk and simmer",
                    "Return chicken to pan and simmer for 10 minutes",
                    "Serve with rice or naan bread"
                ),
                cookingTimeMinutes = 45,
                servings = 4,
                notes = "Can be made spicier by adding chili powder"
            )
        )
    )

    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    fun getRecipe(id: String): Recipe? {
        return _recipes.value.find { it.id == id }
    }

    fun addRecipe(recipe: Recipe) {
        _recipes.value = _recipes.value + recipe
    }

    fun updateRecipe(recipe: Recipe) {
        _recipes.value = _recipes.value.map {
            if (it.id == recipe.id) recipe else it
        }
    }

    fun deleteRecipe(id: String) {
        _recipes.value = _recipes.value.filter { it.id != id }
    }

    fun generateNewId(): String {
        return ((_recipes.value.maxOfOrNull { it.id.toIntOrNull() ?: 0 } ?: 0) + 1).toString()
    }
}
