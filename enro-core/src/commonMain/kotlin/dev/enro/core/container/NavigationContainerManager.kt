package dev.enro.core.container

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import androidx.savedstate.write
import dev.enro.core.EnroException
import dev.enro.core.NavigationContainerKey
import dev.enro.core.controller.NavigationController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

public class NavigationContainerManager {
    private val _containers: MutableSet<NavigationContainer> = mutableSetOf()
    public val containers: Set<NavigationContainer> = _containers

    public val activeContainerState: MutableState<NavigationContainerKey?> = mutableStateOf(null)
    public val activeContainer: NavigationContainer? get() = activeContainerState.value?.let { activeKey ->
        containers.firstOrNull { it.key == activeKey }
    }

    private val mutableActiveContainerFlow = MutableStateFlow<NavigationContainerKey?>(null)
    public val activeContainerFlow: Flow<NavigationContainer?> = mutableActiveContainerFlow
        .map { activeKey ->
            containers.firstOrNull { it.key == activeKey }
        }
        .distinctUntilChanged()

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
        if(activeContainerState.value == null) {
            setActiveContainer(container)
        }
    }

    internal fun removeContainer(container: NavigationContainer) {
        _containers.remove(container)
    }

    internal fun save(outState: SavedState) {
        val container = activeContainer ?: return
        outState.write {
            putSavedState(ACTIVE_CONTAINER_KEY, encodeToSavedState(container.key))
        }
    }

    internal fun restore(savedInstanceState: SavedState?) {
        if (savedInstanceState == null) return
        val activeKeySavedState = savedInstanceState.read { getSavedStateOrNull(ACTIVE_CONTAINER_KEY) }
        if (activeKeySavedState == null) return
        val activeKey = decodeFromSavedState(NavigationContainerKey.Serializer, activeKeySavedState)
        activeContainerState.value = activeKey
        mutableActiveContainerFlow.value = activeKey
    }

    public fun setActiveContainer(containerController: NavigationContainer?) {
        if (containerController == null) {
            activeContainerState.value = null
            mutableActiveContainerFlow.value = null
            return
        }
        val selectedContainer = containers.firstOrNull { it.key == containerController.key }
            ?: throw IllegalStateException("NavigationContainer with id ${containerController.key} is not registered with this NavigationContainerManager")
        activeContainerState.value = selectedContainer.key
        mutableActiveContainerFlow.value = selectedContainer.key
    }

    public companion object {
        private const val ACTIVE_CONTAINER_KEY: String =
            "dev.enro.core.container.NavigationContainerManager.ACTIVE_CONTAINER_KEY"
    }
}