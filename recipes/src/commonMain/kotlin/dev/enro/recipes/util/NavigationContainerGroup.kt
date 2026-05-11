package dev.enro.recipes.util

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

/**
 * A small multiplatform copy of `dev.enro.core.compose.container.NavigationContainerGroup`
 * from `enro-compat`. The original lives in the Android-only compat module, so for the
 * recipes module we mirror the behaviour in common code so the tabs / multiple back stacks
 * recipes can run on every Compose target.
 */
class NavigationContainerGroup internal constructor(
    private val activeContainerState: MutableState<NavigationContainerState>,
    val containers: List<NavigationContainerState>,
) {
    val activeContainer: NavigationContainerState by activeContainerState

    fun setActive(container: NavigationContainerState) {
        activeContainerState.value = container
        container.context.requestActive()
    }
}

@Composable
fun rememberNavigationContainerGroup(
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
        },
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
