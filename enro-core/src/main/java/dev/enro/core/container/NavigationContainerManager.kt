package dev.enro.core.container

import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dev.enro.core.EnroException
import dev.enro.core.NavigationDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

public class NavigationContainerManager {
    private var restoredActiveContainer: String? = null

    private val _containers: MutableSet<NavigationContainer> = mutableSetOf()
    public val containers: Set<NavigationContainer> = _containers

    private val activeContainerState: MutableState<NavigationContainer?> = mutableStateOf(null)
    public val activeContainer: NavigationContainer? get() = activeContainerState.value

    private val mutableActiveContainerFlow = MutableStateFlow<NavigationContainer?>(null)
    public val activeContainerFlow: StateFlow<NavigationContainer?> = mutableActiveContainerFlow
    
    internal fun setActiveContainerById(id: String?) {
        setActiveContainer(containers.firstOrNull { it.id == id })
    }

    internal fun getContainerById(id: String): NavigationContainer? {
        return containers
            .firstOrNull { it.id == id }
    }

    internal fun addContainer(container: NavigationContainer) {
        val existingContainer = getContainerById(container.id)
        if(existingContainer != null && existingContainer !== container) {
            throw EnroException.DuplicateFragmentNavigationContainer("A NavigationContainer with id ${container.id} already exists")
        }

        _containers.add(container)
        restore(container)
        if(activeContainer == null && !container.acceptsDirection(NavigationDirection.Present)) {
            setActiveContainer(container)
        }
    }

    internal fun removeContainer(container: NavigationContainer) {
        _containers.remove(container)
    }

    internal fun save(outState: Bundle) {
        outState.putString(ACTIVE_CONTAINER_KEY, activeContainer?.id)
    }

    internal fun restore(savedInstanceState: Bundle?) {
        if(savedInstanceState == null) return
        restoredActiveContainer = savedInstanceState.getString(ACTIVE_CONTAINER_KEY)
    }

    internal fun restore(container: NavigationContainer) {
        if(restoredActiveContainer == container.id) {
            setActiveContainer(container)
            restoredActiveContainer = null
        }
    }

    public fun setActiveContainer(containerController: NavigationContainer?) {
        if (containerController == null) {
            activeContainerState.value = null
            mutableActiveContainerFlow.value = null
            return
        }
        val selectedContainer = containers.firstOrNull { it.id == containerController.id }
            ?: throw IllegalStateException("NavigationContainer with id ${containerController.id} is not registered with this NavigationContainerManager")
        activeContainerState.value = selectedContainer
        mutableActiveContainerFlow.value = selectedContainer
    }

    public companion object {
        private const val ACTIVE_CONTAINER_KEY: String =
            "dev.enro.core.container.NavigationContainerManager.ACTIVE_CONTAINER_KEY"
    }
}