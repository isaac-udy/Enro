package dev.enro.tests.application.samples.recipes

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.ui.LocalNavigationAnimatedVisibilityScope
import dev.enro.ui.LocalNavigationSharedTransitionScope
import kotlinx.serialization.Serializable

@Serializable
object RecipeList : NavigationKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@NavigationDestination(RecipeList::class)
fun RecipeListScreen() {
    val navigation = navigationHandle<RecipeList>()
    val recipes by RecipeRepository.recipes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Recipes") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navigation.open(CreateRecipe)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Recipe")
            }
        }
    ) { paddingValues ->
        if (recipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No recipes yet",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Tap + to add your first recipe",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recipes) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = {
                            navigation.open(RecipeDetail(recipeId = recipe.id))
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            with(LocalNavigationSharedTransitionScope.current) {
                Text(
                    text = recipe.emoji,
                    fontSize = 48.sp,
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "${recipe.id}:emoji"),
                        animatedVisibilityScope = LocalNavigationAnimatedVisibilityScope.current,
                    )
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState(key = "${recipe.id}:title"),
                            animatedVisibilityScope = LocalNavigationAnimatedVisibilityScope.current,
                        )
                    )
                    if (recipe.description.isNotEmpty()) {
                        Text(
                            text = recipe.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (recipe.cookingTimeMinutes > 0) {
                            Text(
                                text = "⏱️ ${recipe.cookingTimeMinutes} min",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "🍽️ ${recipe.servings} servings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
