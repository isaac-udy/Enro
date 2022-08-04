package dev.enro.core.container

import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.EnroException
import dev.enro.core.NavigationDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NavigationContainerManager {
    private val restoredContainerStates = mutableMapOf<String, NavigationBackstack>()
    private var restoredActiveContainer: String? = null

    private val _containers: MutableSet<NavigationContainer> = mutableSetOf()
    val containers: Set<NavigationContainer> = _containers

    private val activeContainerState: MutableState<NavigationContainer?> = mutableStateOf(null)
    val activeContainer: NavigationContainer? get() = activeContainerState.value

    private val mutableActiveContainerFlow = MutableStateFlow<NavigationContainer?>(null)
    val activeContainerFlow: StateFlow<NavigationContainer?> = mutableActiveContainerFlow
    
    internal fun setActiveContainerById(id: String?) {
        setActiveContainer(containers.firstOrNull { it.id == id })
    }

    internal fun addContainer(container: NavigationContainer) {
        val isExistingContainer = containers
            .any { it.id == container.id }

        if(isExistingContainer) {
            throw EnroException.DuplicateFragmentNavigationContainer("A NavigationContainer with id ${container.id} already exists")
        }

        _containers.add(container)
        restore(container)
        if(activeContainer == null && !container.supportedNavigationDirections.contains(NavigationDirection.Present)) {
            setActiveContainer(container)
        }
    }

    internal fun removeContainer(container: NavigationContainer) {
        _containers.remove(container)
    }

    internal fun save(outState: Bundle) {
        containers.forEach {
            outState.putParcelableArrayList(
                "$BACKSTACK_KEY@${it.id}", ArrayList(it.backstackFlow.value.backstack)
            )
        }

        outState.putStringArrayList(CONTAINER_IDS_KEY, ArrayList(containers.map { it.id }))
        outState.putString(ACTIVE_CONTAINER_KEY, activeContainer?.id)
    }

    internal fun restore(savedInstanceState: Bundle?) {
        if(savedInstanceState == null) return

        savedInstanceState.getStringArrayList(CONTAINER_IDS_KEY)
            .orEmpty()
            .forEach {
                restoredContainerStates[it] = createRestoredBackStack(
                    savedInstanceState
                        .getParcelableArrayList<AnyOpenInstruction>("$BACKSTACK_KEY@$it")
                        .orEmpty()
                )
            }

        restoredActiveContainer = savedInstanceState.getString(ACTIVE_CONTAINER_KEY)
        containers.forEach { restore(it) }
    }

    internal fun restore(container: NavigationContainer) {
        val activeContainer = activeContainer
        val backstack = restoredContainerStates[container.id] ?: return
        restoredContainerStates.remove(container.id)

        container.setBackstack(backstack)
        // TODO this is required because setBackstack sets the active container. Need to fix that...
        setActiveContainer(activeContainer)

        if(restoredActiveContainer == container.id) {
            setActiveContainer(container)
            restoredActiveContainer = null
        }
    }

    fun setActiveContainer(containerController: NavigationContainer?) {
        if(containerController == null) {
            activeContainerState.value = null
            mutableActiveContainerFlow.value = null
            return
        }
        val selectedContainer = containers.firstOrNull { it.id == containerController.id }
            ?: throw IllegalStateException("NavigationContainer with id ${containerController.id} is not registered with this NavigationContainerManager")
        activeContainerState.value = selectedContainer
        mutableActiveContainerFlow.value = selectedContainer
    }

    companion object {
        const val ACTIVE_CONTAINER_KEY = "dev.enro.core.container.NavigationContainerManager.ACTIVE_CONTAINER_KEY"
        const val CONTAINER_IDS_KEY = "dev.enro.core.container.NavigationContainerManager.CONTAINER_IDS_KEY"
        const val BACKSTACK_KEY = "dev.enro.core.container.NavigationContainerManager.BACKSTACK_KEY"
    }
}