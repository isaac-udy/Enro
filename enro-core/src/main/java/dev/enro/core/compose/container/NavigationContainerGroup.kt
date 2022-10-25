package dev.enro.core.compose.container

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import dev.enro.core.containerManager

@Immutable
public data class NavigationContainerGroup(
    public val containers: List<ComposableNavigationContainer>,
    public val activeContainer: ComposableNavigationContainer
)

@Composable
public fun rememberNavigationContainerGroup(vararg containers: ComposableNavigationContainer): NavigationContainerGroup {
    val activeInGroup = rememberSaveable {
        mutableStateOf(containers.first().id)
    }
    val activeContainer = containerManager.activeContainer
    DisposableEffect(activeContainer) {
        val activeId = containers.firstOrNull { it.id == activeContainer?.id }?.id
        if(activeId != null && activeInGroup.value != activeId) {
            activeInGroup.value = activeId
        }
        onDispose {  }
    }

    return remember(activeInGroup.value) {
        NavigationContainerGroup(
            containers = containers.toList(),
            activeContainer = containers.first { it.id == activeInGroup.value }
        )
    }
}