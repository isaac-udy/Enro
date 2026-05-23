/**
 * Enro Recipe: Basic Deep Link
 *
 * Demonstrates how Enro maps URLs to NavigationKeys via the `@NavigationPath`
 * annotation. The annotation processor generates the path bindings and registers
 * them alongside each `@NavigationDestination`, so on platforms with URL
 * integration (currently the browser) the URL bar updates automatically and
 * bookmarked URLs resolve back to the right destination.
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
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
@NavigationPath("/basic-deep-link")
object BasicDeepLinkRecipe : NavigationKey

@Serializable
@NavigationPath("/users/{userId}")
data class UserProfile(val userId: String) : NavigationKey

@Serializable
@NavigationPath("/products/{productId}?source={source?}")
data class ProductDetail(val productId: String, val source: String? = null) : NavigationKey

@Serializable
@NavigationPath("/home")
data object DeepLinkHome : NavigationKey

@Composable
@NavigationDestination(BasicDeepLinkRecipe::class)
fun BasicDeepLinkRecipeScreen() {
    val navigation = navigationHandle<BasicDeepLinkRecipe>()
    RecipeScaffold(
        title = "Basic Deep Link",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(DeepLinkHome.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(DeepLinkHome::class)
fun DeepLinkHomeDestination() {
    val navigation = navigationHandle<DeepLinkHome>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Deep Link Home", style = MaterialTheme.typography.titleLarge)
        Text(
            "Bindings: /home, /users/{userId}, /products/{productId}?source={source?}",
            style = MaterialTheme.typography.bodySmall,
        )
        Button(onClick = { navigation.open(UserProfile("alice")) }) {
            Text("Open user 'alice'")
        }
        Button(onClick = { navigation.open(ProductDetail("shoe-1", "email")) }) {
            Text("Open product 'shoe-1' from email")
        }
        Button(onClick = { navigation.open(ProductDetail("shoe-2")) }) {
            Text("Open product 'shoe-2' (no source)")
        }
    }
}

@Composable
@NavigationDestination(UserProfile::class)
fun UserProfileDestination() {
    val navigation = navigationHandle<UserProfile>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("User Profile", style = MaterialTheme.typography.titleLarge)
        Text("User ID: ${navigation.key.userId}")
        Button(onClick = { navigation.close() }) { Text("Back") }
    }
}

@Composable
@NavigationDestination(ProductDetail::class)
fun ProductDetailDestination() {
    val navigation = navigationHandle<ProductDetail>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Product Detail", style = MaterialTheme.typography.titleLarge)
        Text("Product ID: ${navigation.key.productId}")
        navigation.key.source?.let {
            Text("Source: $it")
        }
        Button(onClick = { navigation.close() }) { Text("Back") }
    }
}
