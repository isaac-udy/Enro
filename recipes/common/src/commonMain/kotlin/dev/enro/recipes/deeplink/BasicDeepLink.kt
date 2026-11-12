/**
 * Enro Recipe: Basic Deep Link
 *
 * Shows two ways to bind a URL path to a NavigationKey, and how to resolve a
 * URL to a key:
 *  - `@NavigationPath` on the recipe entry key. The processor generates and
 *    registers the binding.
 *  - `NavigationPathBinding.createPathBinding(...)` and the
 *    `NavigationPathBinding` constructor, which take the pattern and the
 *    conversion between path and key. [deepLinkModule] registers these
 *    bindings, and `RecipesComponent` installs [deepLinkModule].
 *  - `navigation.getNavigationKeyFromPath(url)` resolves a URL to a key. The
 *    home screen resolves four URLs and opens each resolved key.
 *
 * On the web, the URL bar shows the bound path of the active destination.
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
import androidx.compose.runtime.remember
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
import dev.enro.controller.NavigationModule
import dev.enro.controller.createNavigationModule
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.path.NavigationPathBinding
import dev.enro.path.createPathBinding
import dev.enro.path.getNavigationKeyFromPath
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
@NavigationPath("/basic-deep-link")
object BasicDeepLinkRecipe : NavigationKey

@Serializable
data class UserProfile(val userId: String) : NavigationKey

@Serializable
data class ProductDetail(val productId: String, val source: String? = null) : NavigationKey

@Serializable
data object DeepLinkHome : NavigationKey

// createPathBinding matches each `{name}` in the pattern to the key property
// with the same name. deepLinkModule, below, registers these bindings.

val userProfilePathBinding: NavigationPathBinding<UserProfile> = NavigationPathBinding.createPathBinding(
    pattern = "/users/{userId}",
    propertyOne = UserProfile::userId,
    constructor = ::UserProfile,
)

val productDetailPathBinding: NavigationPathBinding<ProductDetail> = NavigationPathBinding.createPathBinding(
    pattern = "/products/{productId}?source={source?}",
    propertyOne = ProductDetail::productId,
    propertyTwo = ProductDetail::source,
    constructor = ::ProductDetail,
)

val homePathBinding: NavigationPathBinding<DeepLinkHome> = NavigationPathBinding(
    keyType = DeepLinkHome::class,
    pattern = "/home",
    deserialize = { DeepLinkHome },
    serialize = { },
)

val deepLinkModule: NavigationModule = createNavigationModule {
    path(userProfilePathBinding)
    path(productDetailPathBinding)
    path(homePathBinding)
}

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
        Text(
            "Each button resolves its URL with getNavigationKeyFromPath and opens the key.",
            style = MaterialTheme.typography.bodySmall,
        )
        var unresolved by remember { mutableStateOf<String?>(null) }
        val urls = listOf(
            "/users/alice",
            "/products/shoe-1?source=email",
            "/products/shoe-2",
            "/not-a-registered-path",
        )
        urls.forEach { url ->
            Button(onClick = {
                // getNavigationKeyFromPath returns null when no registered binding matches the URL.
                val key = navigation.getNavigationKeyFromPath(url)
                unresolved = if (key == null) url else null
                if (key != null) navigation.open(key)
            }) {
                Text("Open $url")
            }
        }
        unresolved?.let {
            Text(
                "No binding matches $it",
                color = MaterialTheme.colorScheme.error,
            )
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
