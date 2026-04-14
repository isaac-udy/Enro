/**
 * Enro Recipe: Basic Deep Link
 *
 * Nav3 equivalent: "Basic Deep Link" recipe
 * https://nicbell.github.io/nav3/recipes/deep-link
 *
 * Demonstrates how Enro maps URLs to NavigationKeys using NavigationPathBinding,
 * compared to Nav3's deep link handling.
 *
 * Key differences from Nav3:
 * - Nav3 requires manually parsing intents/URIs and mapping them to keys, then manipulating the backstack.
 * - Enro provides NavigationPathBinding, which declaratively maps URL patterns to NavigationKey constructors.
 *   Path parameters ({param}) and query parameters (?param=value) are automatically extracted.
 * - Enro's KSP processor can auto-generate path bindings from @Serializable keys using createPathBinding,
 *   which uses KProperty1 references for type-safe parameter mapping.
 * - Path bindings are registered in NavigationModules alongside destination bindings.
 */
package dev.enro.recipes.deeplink

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.controller.NavigationModule
import dev.enro.controller.createNavigationModule
import dev.enro.navigationHandle
import dev.enro.path.NavigationPathBinding
import dev.enro.path.createPathBinding
import kotlinx.serialization.Serializable

// -- Navigation Keys --

@Serializable
data class UserProfile(val userId: String) : NavigationKey

@Serializable
data class ProductDetail(val productId: String, val source: String? = null) : NavigationKey

@Serializable
data object Home : NavigationKey

// -- Path Bindings --
// These map URL patterns to NavigationKey instances.
// Nav3 has no built-in equivalent; you'd handle deep links in your Activity's onCreate.

// Simple path binding with one parameter:
// URL: /users/abc123 -> UserProfile(userId = "abc123")
val userProfilePathBinding = NavigationPathBinding.createPathBinding(
    pattern = "/users/{userId}",
    propertyOne = UserProfile::userId,
    constructor = ::UserProfile,
)

// Path binding with path param and optional query param:
// URL: /products/shoe-123 -> ProductDetail(productId = "shoe-123", source = null)
// URL: /products/shoe-123?source=email -> ProductDetail(productId = "shoe-123", source = "email")
val productDetailPathBinding = NavigationPathBinding.createPathBinding(
    pattern = "/products/{productId}?source={source}",
    propertyOne = ProductDetail::productId,
    propertyTwo = ProductDetail::source,
    constructor = ::ProductDetail,
)

// Manual path binding for keys without constructor parameters:
val homePathBinding = NavigationPathBinding(
    keyType = Home::class,
    pattern = "/home",
    deserialize = { Home },
    serialize = { },
)

// -- Register path bindings in a NavigationModule --
// Nav3 has no equivalent concept. Path bindings are registered alongside destinations.
val deepLinkModule: NavigationModule = createNavigationModule {
    path(userProfilePathBinding)
    path(productDetailPathBinding)
    path(homePathBinding)
}

// -- Destinations --

@Composable
@NavigationDestination(UserProfile::class)
fun UserProfileDestination() {
    val navigation = navigationHandle<UserProfile>()
    Column {
        Text("User Profile")
        Text("User ID: ${navigation.key.userId}")
        // Whether opened via deep link or normal navigation, the destination
        // receives the same NavigationKey with the same typed properties.
        // This is a key Enro advantage: deep links and in-app navigation
        // use the exact same contract.
    }
}

@Composable
@NavigationDestination(ProductDetail::class)
fun ProductDetailDestination() {
    val navigation = navigationHandle<ProductDetail>()
    Column {
        Text("Product Detail")
        Text("Product ID: ${navigation.key.productId}")
        navigation.key.source?.let {
            Text("Source: $it")
        }
    }
}

@Composable
@NavigationDestination(Home::class)
fun HomeDestination() {
    Column {
        Text("Home Screen")
    }
}
