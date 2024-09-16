package dev.enro.core.compose.container

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import dev.enro.core.containerManager

/**
 * A NavigationContainerGroup is a group of [ComposableNavigationContainer]s that are managed together, with one of those
 * containers being the active container. This is useful for managing multiple navigation containers that are in the same
 * context/ContainerManager, where the active container within the group may or may not be the active container in the ContainerManager.
 *
 * In most cases, if a NavigationContainerGroup contains the all the containers that are registered with the ContainerManager,
 * it will not be required to use a NavigationContainerGroup to implement the desired behaviour. In these cases, you could
 * directly use the ContainerManager to manage the active container within the group, but it may still be useful to use a
 * NavigationContainerGroup for the simplified syntax, explicit definition of behaviour, or where ordering of containers is important.
 */
@Immutable
public data class NavigationContainerGroup(
    public val containers: List<ComposableNavigationContainer>,
    public val activeContainer: ComposableNavigationContainer
)

/**
 * This function creates and remembers a NavigationContainerGroup.
 *
 * @see [NavigationContainerGroup]
 *
 * @param containers The containers that are part of the NavigationContainerGroup
 * @param setActiveInContainerManager Whether the first container in the list should be set as the active container in the associated
 * NavigationContainerManager when this NavigationContainerGroup is created. The first container will always be the active
 * container within the NavigationContainerGroup, but in cases where multiple NavigationContainerGroups are created in the
 * context of the same NavigationContainerManager, it is useful to choose one NavigationContainerGroup to start as
 * active in the NavigationContainerManager. This defaults to true.
 */
@Composable
public fun rememberNavigationContainerGroup(
    vararg containers: ComposableNavigationContainer,
    setActiveInContainerManager: Boolean = true,
): NavigationContainerGroup {
    val containerManager = containerManager
    val activeInGroup = rememberSaveable {
        val firstContainer = containers.first()
        if (setActiveInContainerManager) { containerManager.setActiveContainer(firstContainer) }

        mutableStateOf(firstContainer.key)
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