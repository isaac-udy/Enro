/**
 * Enro Recipe: Basic Deep Link
 *
 * Demonstrates two ways of mapping URLs to NavigationKeys:
 *  - `@NavigationPath` on the recipe entry key, which is what the web platform
 *    plugin actually uses to drive the URL bar. Only root-level destinations
 *    participate in URL routing today; see the web platform docs.
 *  - The manual `NavigationPathBinding.createPathBinding(...)` API, shown
 *    below as illustrative code. The bindings here are NOT registered with
 *    the controller — they're examples of the hand-written form for cases
 *    where you want full control over deserialize/serialize.
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
import dev.enro.controller.NavigationModule
import dev.enro.controller.createNavigationModule
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.path.NavigationPathBinding
import dev.enro.path.createPathBinding
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

// Illustrative: hand-written path bindings using the createPathBinding API.
// Not registered with the controller — this recipe shows the shape of the API.

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
            "Illustrative bindings: /home, /users/{userId}, /products/{productId}?source={source?}",
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
