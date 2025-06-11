package dev.enro.tests.application.samples.recipes

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.ui.LocalNavigationAnimatedVisibilityScope
import dev.enro.ui.LocalNavigationSharedTransitionScope
import kotlinx.serialization.Serializable

@Serializable
data class RecipeDetail(
    val recipeId: String,
) : NavigationKey

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
@NavigationDestination(RecipeDetail::class)
fun RecipeDetailScreen() {
    val navigation = navigationHandle<RecipeDetail>()
    val recipe = RecipeRepository.getRecipe(navigation.key.recipeId)
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (recipe == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Recipe not found")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    with(LocalNavigationSharedTransitionScope.current) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = recipe.title,
                                modifier = Modifier.fillMaxWidth().sharedElement(
                                    rememberSharedContentState(key = "${recipe.id}:title"),
                                    animatedVisibilityScope = LocalNavigationAnimatedVisibilityScope.current,
                                )
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            navigation.open(EditRecipe(recipeId = recipe.id))
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Recipe")
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Recipe")
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
            // Description
            if (recipe.description.isNotEmpty()) {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = recipe.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (recipe.cookingTimeMinutes > 0) {
                    Card(
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⏱️",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = "${recipe.cookingTimeMinutes} min",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Card(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🍽️",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "${recipe.servings} servings",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Ingredients
            if (recipe.ingredients.isNotEmpty()) {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Ingredients",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        recipe.ingredients.forEach { ingredient ->
                            Text(
                                text = "• $ingredient",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Instructions
            if (recipe.instructions.isNotEmpty()) {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Instructions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        recipe.instructions.forEachIndexed { index, instruction ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = instruction,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Notes
            if (recipe.notes.isNotEmpty()) {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = recipe.notes,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recipe?") },
            text = { Text("Are you sure you want to delete \"${recipe.title}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        RecipeRepository.deleteRecipe(recipe.id)
                        navigation.close()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
