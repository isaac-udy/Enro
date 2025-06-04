package dev.enro.core.compose.container

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import dev.enro.ui.NavigationContainerState


public class NavigationContainerGroup {
    public val containers: List<NavigationContainerState> = TODO()
    public val activeContainer: NavigationContainerState = TODO()

    public fun setActive(container: NavigationContainerState) {}
}

@Composable
public fun rememberNavigationContainerGroup(
    vararg containers: NavigationContainerState,
) : NavigationContainerGroup {
    TODO("FINISH")
    val group = rememberSaveable(

    ) {
        NavigationContainerGroup()
    }
    val activeInGroup = rememberSaveable {
        val firstContainer = containers.first()
        mutableStateOf(firstContainer.key)
    }
    return group
}