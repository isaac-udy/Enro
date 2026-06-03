package dev.enro.tests.application.samples.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.navigationHandle
import kotlinx.serialization.Serializable

@Serializable
data class EditRecipe(
    val recipeId: String,
) : NavigationKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@NavigationDestination(EditRecipe::class)
fun EditRecipeScreen() {
    val navigation = navigationHandle<EditRecipe>()
    val existingRecipe = navigation.key.recipeId.let { RecipeRepository.getRecipe(it) }

    var title by remember { mutableStateOf(existingRecipe?.title ?: "") }
    var emoji by remember { mutableStateOf(existingRecipe?.emoji ?: "🍽️") }
    var description by remember { mutableStateOf(existingRecipe?.description ?: "") }
    var cookingTime by remember { mutableStateOf(existingRecipe?.cookingTimeMinutes?.toString() ?: "") }
    var servings by remember { mutableStateOf(existingRecipe?.servings?.toString() ?: "4") }
    var ingredients by remember { mutableStateOf(existingRecipe?.ingredients ?: emptyList()) }
    var instructions by remember { mutableStateOf(existingRecipe?.instructions ?: emptyList()) }
    var notes by remember { mutableStateOf(existingRecipe?.notes ?: "") }

    var newIngredient by remember { mutableStateOf("") }
    var newInstruction by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Recipe") },
                navigationIcon = {
                    IconButton(onClick = { navigation.close() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val recipe = Recipe(
                                id = existingRecipe?.id ?: RecipeRepository.generateNewId(),
                                title = title.trim(),
                                emoji = emoji.trim().ifEmpty { "🍽️" },
                                description = description.trim(),
                                ingredients = ingredients.filter { it.isNotBlank() },
                                instructions = instructions.filter { it.isNotBlank() },
                                cookingTimeMinutes = cookingTime.toIntOrNull() ?: 0,
                                servings = servings.toIntOrNull() ?: 4,
                                notes = notes.trim()
                            )

                            RecipeRepository.updateRecipe(recipe)
                            navigation.close()
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Information
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Basic Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = emoji,
                            onValueChange = { emoji = it },
                            label = { Text("Emoji") },
                            modifier = Modifier.width(100.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Recipe Title*") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = cookingTime,
                            onValueChange = { cookingTime = it },
                            label = { Text("Cooking Time (min)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = servings,
                            onValueChange = { servings = it },
                            label = { Text("Servings") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }
            }

            // Ingredients
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Ingredients",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    ingredients.forEachIndexed { index, ingredient ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "•",
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            OutlinedTextField(
                                value = ingredient,
                                onValueChange = { newValue ->
                                    ingredients = ingredients.toMutableList().apply {
                                        this[index] = newValue
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            IconButton(
                                onClick = {
                                    ingredients = ingredients.filterIndexed { i, _ -> i != index }
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove")
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newIngredient,
                            onValueChange = { newIngredient = it },
                            label = { Text("Add ingredient") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(
                            onClick = {
                                if (newIngredient.isNotBlank()) {
                                    ingredients = ingredients + newIngredient
                                    newIngredient = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                }
            }

            // Instructions
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    instructions.forEachIndexed { index, instruction ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${index + 1}.",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                            OutlinedTextField(
                                value = instruction,
                                onValueChange = { newValue ->
                                    instructions = instructions.toMutableList().apply {
                                        this[index] = newValue
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                minLines = 2
                            )
                            IconButton(
                                onClick = {
                                    instructions = instructions.filterIndexed { i, _ -> i != index }
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove")
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        OutlinedTextField(
                            value = newInstruction,
                            onValueChange = { newInstruction = it },
                            label = { Text("Add instruction") },
                            modifier = Modifier.weight(1f),
                            minLines = 2
                        )
                        IconButton(
                            onClick = {
                                if (newInstruction.isNotBlank()) {
                                    instructions = instructions + newInstruction
                                    newInstruction = ""
                                }
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                }
            }

            // Notes
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Additional notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }
        }
    }
}
