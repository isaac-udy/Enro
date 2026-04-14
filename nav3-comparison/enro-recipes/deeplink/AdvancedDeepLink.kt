/**
 * Enro Recipe: Advanced Deep Link
 *
 * Nav3 equivalent: "Advanced Deep Link" recipe
 * https://nicbell.github.io/nav3/recipes/advanced-deep-link
 *
 * Demonstrates how Enro handles synthetic backstacks for deep links, ensuring
 * the user has a proper navigation history when arriving via a deep link.
 *
 * Key differences from Nav3:
 * - Nav3's advanced deep link recipe manually builds a synthetic backstack by pre-populating
 *   the backstack list before rendering NavDisplay.
 * - Enro handles this via NavigationInterceptors. When a deep link opens a screen that
 *   should have a parent in the backstack, an interceptor can prepend the parent screens.
 * - Alternatively, the container can be initialized with a pre-built backstack that includes
 *   the synthetic parent screens.
 * - Enro's NavigationPathBindings work with the interceptor system, so deep link resolution
 *   and synthetic backstack construction can be fully declarative.
 */
package dev.enro.recipes.deeplink

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.controller.createNavigationModule
import dev.enro.interceptor.builder.navigationInterceptor
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.path.NavigationPathBinding
import dev.enro.path.createPathBinding
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

// -- Navigation Keys --

@Serializable
data object AppHome : NavigationKey

@Serializable
data class Category(val categoryId: String) : NavigationKey

@Serializable
data class Article(val articleId: String, val categoryId: String) : NavigationKey

// -- Path Bindings --

val categoryPathBinding = NavigationPathBinding.createPathBinding(
    pattern = "/categories/{categoryId}",
    propertyOne = Category::categoryId,
    constructor = ::Category,
)

val articlePathBinding = NavigationPathBinding.createPathBinding(
    pattern = "/categories/{categoryId}/articles/{articleId}",
    propertyOne = Article::articleId,
    propertyTwo = Article::categoryId,
    constructor = ::Article,
)

// -- Synthetic Backstack via Interceptor --
// When an Article is opened directly (e.g., from a deep link), we want the backstack
// to include AppHome and the parent Category, so pressing back navigates logically.
//
// Nav3 equivalent: Manually constructing the backstack in your deep link handler:
//   backStack.addAll(listOf(AppHome, Category(categoryId), Article(articleId)))
//
// Enro: Use a NavigationInterceptor that detects when Article is opened without
// a parent Category on the backstack, and prepends the synthetic history.

val syntheticBackstackInterceptor = navigationInterceptor {
    onOpened<Article> {
        // The interceptor could check the current backstack state and decide
        // whether to add synthetic entries. For deep links, the backstack is typically
        // empty or has only the root.
        //
        // Option 1: Always continue (let the container handle it)
        continueWithOpen()

        // Option 2: Replace with a synthetic backstack (shown in the host below)
    }
}

// -- Register in module --
val advancedDeepLinkModule = createNavigationModule {
    path(categoryPathBinding)
    path(articlePathBinding)
    interceptor(syntheticBackstackInterceptor)
}

// -- Host with Synthetic Backstack --
// The most common approach for deep links in Enro is to construct the initial backstack
// with the full synthetic history when handling the deep link intent.

@Composable
fun DeepLinkHost(deepLinkArticleId: String?, deepLinkCategoryId: String?) {
    // Build the initial backstack based on whether we're coming from a deep link
    val initialBackstack = if (deepLinkArticleId != null && deepLinkCategoryId != null) {
        // Deep link: build synthetic backstack with full history
        // Nav3 equivalent: backStack.addAll(listOf(AppHome, Category(...), Article(...)))
        backstackOf(
            AppHome.asInstance(),
            Category(deepLinkCategoryId).asInstance(),
            Article(deepLinkArticleId, deepLinkCategoryId).asInstance(),
        )
    } else {
        // Normal launch: just the home screen
        backstackOf(AppHome.asInstance())
    }

    val container = rememberNavigationContainer(
        backstack = initialBackstack,
    )

    // The user sees the Article screen. Pressing back goes to Category, then Home.
    // This works identically whether the user navigated manually or arrived via deep link.
    NavigationDisplay(state = container)
}

// -- Destinations --

@Composable
@NavigationDestination(AppHome::class)
fun AppHomeDestination() {
    val navigation = navigationHandle<AppHome>()
    Column {
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
    Column {
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
    Column {
        Text("Article: ${navigation.key.articleId}")
        Text("Category: ${navigation.key.categoryId}")
        Button(onClick = { navigation.close() }) {
            Text("Back")
        }
    }
}
