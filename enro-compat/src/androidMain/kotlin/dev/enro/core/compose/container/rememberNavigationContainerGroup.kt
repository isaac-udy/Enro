package dev.enro.core.compose.container

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import dev.enro.context.ContainerContext
import dev.enro.ui.LocalNavigationContext
import dev.enro.ui.NavigationContainerState


public class NavigationContainerGroup(
    private val activeContainerState: MutableState<NavigationContainerState>,
    public val containers: List<NavigationContainerState>,
) {
    public val activeContainer: NavigationContainerState by activeContainerState

    public fun setActive(container: NavigationContainerState) {
        activeContainerState.value = container
        container.context.requestActive()
    }
}

@Composable
public fun rememberNavigationContainerGroup(
    vararg containers: NavigationContainerState,
): NavigationContainerGroup {
    val containerReference = containers.map { it.container }
    val activeContainer = rememberSaveable(
        containerReference,
        saver = object : Saver<MutableState<NavigationContainerState>, String> {
            override fun restore(value: String): MutableState<NavigationContainerState>? {
                return containers.firstOrNull { it.container.key.name == value }
                    ?.let { mutableStateOf(it) }
            }

            override fun SaverScope.save(value: MutableState<NavigationContainerState>): String? {
                return value.value.container.key.name
            }
        }
    ) {
        mutableStateOf(containers.first())
    }
    val group = remember(containerReference) {
        NavigationContainerGroup(
            activeContainer,
            containers.toList(),
        )
    }
    val locallyActiveChild = LocalNavigationContext.current.activeChild
    LaunchedEffect(locallyActiveChild) {
        if (locallyActiveChild !is ContainerContext) return@LaunchedEffect
        if (locallyActiveChild != group.activeContainer.context) {
            containers.firstOrNull { it.context == locallyActiveChild }
                ?.let { group.setActive(it) }
        }
    }
    return group
}