// Snippets for docs/ghpages/docs/platform/web.md
package dev.enro.docs.platform.web

import dev.enro.NavigationKey
import dev.enro.annotations.NavigationComponent
import dev.enro.annotations.NavigationPath
import dev.enro.controller.NavigationComponentConfiguration
import kotlinx.serialization.Serializable

@Serializable
data object Home : NavigationKey

@NavigationComponent
object MyComponent : NavigationComponentConfiguration()

// region docs:web/path
@Serializable
@NavigationPath("/products/{productId}?source={source?}")
data class ProductDetail(
    val productId: String,
    val source: String? = null,
) : NavigationKey
// endregion
