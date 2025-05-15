package dev.enro.tests.application.compose

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.enro.annotations.NavigationDestination
import dev.enro.annotations.NavigationPath
import dev.enro.core.NavigationKey
import dev.enro.core.compose.navigationHandle
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.serialization.Serializable

@Serializable
@NavigationPath("/composable-with-path/{id}?name={name}&title={title?}")
data class ComposableNavigationPath(
    val id: String,
    val name: String,
    val title: String? = null,
) : NavigationKey.SupportsPresent {

    @NavigationPath("/composable-with-path?title={title?}")
    constructor(
        title: String?,
    ): this(
        id = "default-id",
        name = "default-name",
        title = title
    )
}

@Composable
@NavigationDestination(ComposableNavigationPath::class)
fun ComposableNavigationPathDestination() {
    val navigation = navigationHandle<ComposableNavigationPath>()
    TitledColumn("Composable Navigation Path") {
        Text("id: ${navigation.key.id}")
        Text("name: ${navigation.key.name}")
        Text("title: ${navigation.key.title}")
    }
}