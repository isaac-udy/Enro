/**
 * Enro Recipe: Advanced Deep Link
 *
 * Shows a synthetic backstack for a deep link. `updateBackstack` replaces the
 * container's backstack with the target screen on top of the parent screens
 * of the target, so Back from the target opens a parent screen.
 *
 * The recipe entry key has a `@NavigationPath`, so on the web the URL bar
 * shows `/advanced-deep-link` while the recipe is active. `Category` and
 * `Article` have no registered path binding and do not change the URL.
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
import dev.enro.path.NavigationPathBinding
import dev.enro.path.createPathBinding
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
@NavigationPath("/advanced-deep-link")
object AdvancedDeepLinkRecipe : NavigationKey

@Serializable
data object AppHome : NavigationKey

@Serializable
data class Category(val categoryId: String) : NavigationKey

@Serializable
data class Article(val articleId: String, val categoryId: String) : NavigationKey

// Examples of nested patterns with createPathBinding. No module registers
// these two bindings with the controller.

val categoryPathBinding: NavigationPathBinding<Category> = NavigationPathBinding.createPathBinding(
    pattern = "/categories/{categoryId}",
    propertyOne = Category::categoryId,
    constructor = ::Category,
)

val articlePathBinding: NavigationPathBinding<Article> = NavigationPathBinding.createPathBinding(
    pattern = "/categories/{categoryId}/articles/{articleId}",
    propertyOne = Article::articleId,
    propertyTwo = Article::categoryId,
    constructor = ::Article,
)

@Composable
@NavigationDestination(AdvancedDeepLinkRecipe::class)
fun AdvancedDeepLinkRecipeScreen() {
    val navigation = navigationHandle<AdvancedDeepLinkRecipe>()

    RecipeScaffold(
        title = "Advanced Deep Link",
        navigation = navigation,
    ) { modifier ->
        // `backstack` is the backstack for a normal launch. The buttons below
        // replace the backstack of this container to simulate a deep link.
        val container = rememberNavigationContainer(
            backstack = backstackOf(AppHome.asInstance()),
        )
        Column(modifier = modifier) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Simulate launch type:",
                    style = MaterialTheme.typography.titleSmall,
                )
                Button(onClick = {
                    container.updateBackstack { backstackOf(AppHome.asInstance()) }
                }) {
                    Text("Normal launch")
                }
                Button(onClick = {
                    // The new backstack contains the article's parent screens below
                    // the article, so Back opens the category and then home.
                    // region docs:deep-links/synthetic-backstack
                    container.updateBackstack {
                        backstackOf(
                            AppHome.asInstance(),
                            Category("tech").asInstance(),
                            Article("deep-link-article", "tech").asInstance(),
                        )
                    }
                    // endregion
                }) {
                    Text("Deep link to /categories/tech/articles/deep-link-article")
                }
            }

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
