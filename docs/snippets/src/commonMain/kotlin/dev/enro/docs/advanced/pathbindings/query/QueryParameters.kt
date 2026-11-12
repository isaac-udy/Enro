// Snippets for docs/ghpages/docs/advanced/path-bindings.md
package dev.enro.docs.advanced.pathbindings.query

import dev.enro.NavigationKey
import dev.enro.annotations.NavigationPath
import kotlinx.serialization.Serializable

// region docs:path-bindings/query
@Serializable
@NavigationPath("/products/{productId}?source={source?}&campaign={campaign?}")
data class ProductDetail(
    val productId: String,
    val source: String? = null,
    val campaign: String? = null,
) : NavigationKey
// endregion
