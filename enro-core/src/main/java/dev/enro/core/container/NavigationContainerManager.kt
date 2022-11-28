package dev.enro.core.container

import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dev.enro.core.EnroException
import dev.enro.core.NavigationContainerKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

public class NavigationContainerManager {
    private var restoredActiveContainer: NavigationContainerKey? = null

    private val _containers: MutableSet<NavigationContainer> = mutableSetOf()
    public val containers: Set<NavigationContainer> = _containers

    private val activeContainerState: MutableState<NavigationContainer?> = mutableStateOf(null)
    public val activeContainer: NavigationContainer? get() = activeContainerState.value

    private val mutableActiveContainerFlow = MutableStateFlow<NavigationContainer?>(null)
    public val activeContainerFlow: StateFlow<NavigationContainer?> = mutableActiveContainerFlow
    
    internal fun setActiveContainerByKey(key: NavigationContainerKey?) {
        setActiveContainer(containers.firstOrNull { it.key == key })
    }

    internal fun getContainer(key: NavigationContainerKey): NavigationContainer? {
        return containers
            .firstOrNull { it.key == key }
    }

    internal fun addContainer(container: NavigationContainer) {
        val existingContainer = getContainer(container.key)
        if(existingContainer != null && existingContainer !== container) {
            throw EnroException.DuplicateFragmentNavigationContainer("A NavigationContainer with key ${container.key} already exists")
        }

        _containers.add(container)
        restore(container)
        if(activeContainer == null) {
            setActiveContainer(container)
        }
    }

    internal fun removeContainer(container: NavigationContainer) {
        _containers.remove(container)
    }

    internal fun save(outState: Bundle) {
        outState.putParcelable(ACTIVE_CONTAINER_KEY, activeContainer?.key)
    }

    internal fun restore(savedInstanceState: Bundle?) {
        if(savedInstanceState == null) return
        restoredActiveContainer = savedInstanceState.getParcelable(ACTIVE_CONTAINER_KEY)
    }

    internal fun restore(container: NavigationContainer) {
        if(restoredActiveContainer == container.key) {
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
        val selectedContainer = containers.firstOrNull { it.key == containerController.key }
            ?: throw IllegalStateException("NavigationContainer with id ${containerController.key} is not registered with this NavigationContainerManager")
        activeContainerState.value = selectedContainer
        mutableActiveContainerFlow.value = selectedContainer
    }

    public companion object {
        private const val ACTIVE_CONTAINER_KEY: String =
            "dev.enro.core.container.NavigationContainerManager.ACTIVE_CONTAINER_KEY"
    }
}