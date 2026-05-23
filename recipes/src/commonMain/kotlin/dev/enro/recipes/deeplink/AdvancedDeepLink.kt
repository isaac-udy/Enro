/**
 * Enro Recipe: Advanced Deep Link
 *
 * Demonstrates how Enro handles synthetic backstacks for deep links by
 * pre-populating the backstack so the user has a sensible navigation history.
 * Path bindings are declared with `@NavigationPath`, so on the web the URL
 * bar reflects the deep-link state and bookmarks resolve back to the right
 * destination.
 */
@file:OptIn(ExperimentalEnroApi::class)

package dev.enro.recipes.deeplink

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.annotations.NavigationDestination
import dev.enro.annotations.NavigationPath
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
@NavigationPath("/advanced-deep-link")
object AdvancedDeepLinkRecipe : NavigationKey

@Serializable
@NavigationPath("/app-home")
data object AppHome : NavigationKey

@Serializable
@NavigationPath("/categories/{categoryId}")
data class Category(val categoryId: String) : NavigationKey

@Serializable
@NavigationPath("/categories/{categoryId}/articles/{articleId}")
data class Article(val articleId: String, val categoryId: String) : NavigationKey

@Composable
@NavigationDestination(AdvancedDeepLinkRecipe::class)
fun AdvancedDeepLinkRecipeScreen() {
    val navigation = navigationHandle<AdvancedDeepLinkRecipe>()

    // Toggle between "normal" launch and "deep link" launch to demonstrate the
    // synthetic backstack approach.
    var deepLinkArticleId by rememberSaveable { mutableStateOf<String?>(null) }
    var deepLinkCategoryId by rememberSaveable { mutableStateOf<String?>(null) }

    RecipeScaffold(
        title = "Advanced Deep Link",
        navigation = navigation,
    ) { modifier ->
        Column(modifier = modifier) {
            // Controls to "simulate" a deep link arrival
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Simulate launch type:",
                    style = MaterialTheme.typography.titleSmall,
                )
                Button(onClick = {
                    deepLinkArticleId = null
                    deepLinkCategoryId = null
                }) {
                    Text("Normal launch")
                }
                Button(onClick = {
                    deepLinkArticleId = "deep-link-article"
                    deepLinkCategoryId = "tech"
                }) {
                    Text("Deep link to /categories/tech/articles/deep-link-article")
                }
            }

            // Build the initial backstack based on whether we're "deep linked".
            val initialBackstack = if (deepLinkArticleId != null && deepLinkCategoryId != null) {
                backstackOf(
                    AppHome.asInstance(),
                    Category(deepLinkCategoryId!!).asInstance(),
                    Article(deepLinkArticleId!!, deepLinkCategoryId!!).asInstance(),
                )
            } else {
                backstackOf(AppHome.asInstance())
            }

            // We use the deepLink ids in the container key to force re-creation when
            // toggling launch types.
            val container = rememberNavigationContainer(
                backstack = initialBackstack,
            )
            NavigationDisplay(state = container)
        }
    }
}

@Composable
@NavigationDestination(AppHome::class)
fun AppHomeDestination() {
    val navigation = navigationHandle<AppHome>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Home")
        Button(onClick = { navigation.open(Category("tech")) }) {
            Text("Tech Category")
        }
        Button(onClick = { navigation.open(Category("science")) }) {
            Text("Science Category")
        }
    }
}

@Composable
@NavigationDestination(Category::class)
fun CategoryDestination() {
    val navigation = navigationHandle<Category>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Category: ${navigation.key.categoryId}")
        Button(onClick = {
            navigation.open(
                Article(
                    articleId = "article-1",
                    categoryId = navigation.key.categoryId,
                )
            )
        }) {
            Text("Read Article 1")
        }
        Button(onClick = { navigation.close() }) {
            Text("Back")
        }
    }
}

@Composable
@NavigationDestination(Article::class)
fun ArticleDestination() {
    val navigation = navigationHandle<Article>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Article: ${navigation.key.articleId}")
        Text("Category: ${navigation.key.categoryId}")
        Button(onClick = { navigation.close() }) {
            Text("Back")
        }
    }
}
