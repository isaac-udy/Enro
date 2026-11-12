// Snippets for docs/ghpages/docs/advanced/path-bindings.md
package dev.enro.docs.advanced.pathbindings.valueclass

import dev.enro.NavigationKey
import dev.enro.annotations.NavigationPath
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

// region docs:path-bindings/value-class
@JvmInline
@Serializable
value class ProductId(val value: String)

@Serializable
@NavigationPath("/products/{productId}")
data class ProductDetail(
    val productId: ProductId,
) : NavigationKey
// endregion
