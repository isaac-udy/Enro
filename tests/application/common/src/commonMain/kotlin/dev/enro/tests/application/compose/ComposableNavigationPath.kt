package dev.enro.tests.application.compose

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.annotations.NavigationDestination
import dev.enro.annotations.NavigationPath
import dev.enro.navigationHandle
import dev.enro.path.PathData
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class PathItemId(val value: String)

@OptIn(ExperimentalEnroApi::class)
@Serializable
@NavigationPath("/composable-with-path/{id}?name={name}&title={title?}")
@NavigationPath.FromBinding(ComposableNavigationPath.Default::class)
data class ComposableNavigationPath(
    val id: PathItemId,
    val name: String,
    val title: String? = null,
) : NavigationKey {

    object Default : NavigationKey.PathBinding<ComposableNavigationPath> {
        override val pattern: String = "/composable-with-path?title={title?}"

        override fun deserialize(data: PathData): ComposableNavigationPath {
            return ComposableNavigationPath(
                id = PathItemId("default-id"),
                name = "default-name",
                title = data.optional("title"),
            )
        }

        override fun serialize(builder: PathData.Builder, key: ComposableNavigationPath) {
            key.title?.let { builder.set("title", it) }
        }
    }
}

@Composable
@NavigationDestination(ComposableNavigationPath::class)
fun ComposableNavigationPathDestination() {
    val navigation = navigationHandle<ComposableNavigationPath>()
    TitledColumn("Composable Navigation Path") {
        Text("id: ${navigation.key.id.value}")
        Text("name: ${navigation.key.name}")
        Text("title: ${navigation.key.title}")
    }
}
