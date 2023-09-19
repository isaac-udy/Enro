package dev.enro.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationHost
import dev.enro.core.NavigationKey
import dev.enro.core.getNavigationHandle
import dev.enro.core.rootContext
import dev.enro.destination.compose.navigationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.parcelize.Parcelize

@Parcelize
class Backstacks : NavigationKey.SupportsPush

@NavigationDestination(Backstacks::class)
@Composable
fun ShowBackstackDestination() = Surface {
    val navigationContext = navigationContext
    val rootContextItem = remember {
        mutableStateOf(ContextItem(Backstacks()))
    }

    LaunchedEffect(Unit) {
        while(isActive) {
            val rootContext = navigationContext.rootContext()
            rootContextItem.value = createContextItem(rootContext)
            delay(256)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Backstacks",
            style = MaterialTheme.typography.h4
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                .padding(8.dp)
        ) {
            RenderContextItem(contextItem = rootContextItem.value)
        }
    }
}

@Composable
fun RenderContextItem(contextItem: ContextItem) {
    Text(text = "${contextItem.navigationKey::class.java.simpleName}")
    if (contextItem.containers.isEmpty()) return
    contextItem.containers
        .filter { it.backstack.isNotEmpty() }
        .forEach {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                    .padding(8.dp)
            ) {
                RenderContainerItem(it)
            }
    }
}

@Composable
fun ColumnScope.RenderContainerItem(containerItem: ContainerItem) {
    val backstack = containerItem.backstack.dropLast(
        if(containerItem.activeContext != null) 1 else 0
    )
    backstack.forEach {
        Text("${it::class.java.simpleName}")
    }
    containerItem.activeContext?.let {
        RenderContextItem(contextItem = it)
    }
}


@OptIn(AdvancedEnroApi::class)
fun createContextItem(navigationContext: NavigationContext<*>): ContextItem {
    val navigationKey = navigationContext.getNavigationHandle().key
    if (navigationContext.contextReference is NavigationHost) {
        runCatching {
            val child = navigationContext.containerManager.activeContainer!!.childContext!!
            return createContextItem(child)
        }
    }
    return ContextItem(
        navigationKey = navigationKey,
        containers = createContainerItems(navigationContext)
    )
}

fun createContainerItems(navigationContext: NavigationContext<*>) : List<ContainerItem> {
    return navigationContext.containerManager.containers.map { container ->
        ContainerItem(
            activeContext = container.childContext?.let { childContext -> createContextItem(childContext) },
            backstack = container.backstack.map { it.navigationKey }
        )
    }
}

data class ContextItem(
    val navigationKey: NavigationKey,
    val containers: List<ContainerItem> = emptyList()
)

data class ContainerItem(
    val activeContext: ContextItem?,
    val backstack: List<NavigationKey>,
)