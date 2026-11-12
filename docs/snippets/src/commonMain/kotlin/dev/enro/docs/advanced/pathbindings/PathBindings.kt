// Snippets for docs/ghpages/docs/advanced/path-bindings.md
package dev.enro.docs.advanced.pathbindings

import dev.enro.NavigationKey
import dev.enro.annotations.NavigationPath
import dev.enro.path.PathData
import kotlinx.serialization.Serializable

@Serializable
data object Home : NavigationKey

// region docs:path-bindings/simple
@Serializable
@NavigationPath("/products/{productId}")
data class ProductDetail(
    val productId: String,
) : NavigationKey
// endregion

// region docs:path-bindings/from-binding
@Serializable
@NavigationPath("/items/{id}?name={name}&title={title?}")
@NavigationPath.FromBinding(MyKey.Default::class)
data class MyKey(
    val id: String,
    val name: String,
    val title: String? = null,
) : NavigationKey {

    object Default : NavigationKey.PathBinding<MyKey> {
        // Fallback shape — used when the URL is just /items with a title.
        override val pattern: String = "/items?title={title?}"

        override fun deserialize(data: PathData): MyKey {
            return MyKey(
                id = "default-id",
                name = "default-name",
                title = data.optional("title"),
            )
        }

        override fun serialize(builder: PathData.Builder, key: MyKey) {
            key.title?.let { builder.set("title", it) }
        }
    }
}
// endregion
