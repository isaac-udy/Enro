package dev.enro.tests.application.samples.recipes

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: String,
    val title: String,
    val emoji: String = "🍽️",
    val description: String = "",
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val cookingTimeMinutes: Int = 0,
    val servings: Int = 4,
    val notes: String = "",
)
