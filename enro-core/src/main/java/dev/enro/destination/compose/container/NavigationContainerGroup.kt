package dev.enro.destination.compose.container

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import dev.enro.destination.activity.containerManager
import dev.enro.destination.compose.containerManager

@Immutable
public data class NavigationContainerGroup(
    public val containers: List<ComposableNavigationContainer>,
    public val activeContainer: ComposableNavigationContainer
)

@Composable
public fun rememberNavigationContainerGroup(vararg containers: ComposableNavigationContainer): NavigationContainerGroup {
    val activeInGroup = rememberSaveable {
        mutableStateOf(containers.first().key)
    }
    val activeContainer = containerManager.activeContainer
    DisposableEffect(activeContainer) {
        val activeId = containers.firstOrNull { it.key == activeContainer?.key }?.key
        if(activeId != null && activeInGroup.value != activeId) {
            activeInGroup.value = activeId
        }
        onDispose {  }
    }

    return remember(activeInGroup.value) {
        NavigationContainerGroup(
            containers = containers.toList(),
            activeContainer = containers.first { it.key == activeInGroup.value }
        )
    }
}