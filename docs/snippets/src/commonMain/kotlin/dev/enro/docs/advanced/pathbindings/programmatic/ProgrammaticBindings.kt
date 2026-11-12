// Snippets for docs/ghpages/docs/advanced/path-bindings.md
package dev.enro.docs.advanced.pathbindings.programmatic

import dev.enro.NavigationKey
import dev.enro.controller.createNavigationModule
import dev.enro.path.NavigationPathBinding
import dev.enro.path.createPathBinding
import kotlinx.serialization.Serializable

@Serializable
data class ProductDetail(
    val productId: String,
    val source: String? = null,
) : NavigationKey

// region docs:path-bindings/programmatic
val productDetailBinding: NavigationPathBinding<ProductDetail> =
    NavigationPathBinding.createPathBinding(
        pattern = "/products/{productId}?source={source?}",
        propertyOne = ProductDetail::productId,
        propertyTwo = ProductDetail::source,
        constructor = ::ProductDetail,
    )

val pathsModule = createNavigationModule {
    path(productDetailBinding)
}
// endregion
