package dev.enro.ui.decorators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.savedState
import androidx.savedstate.write

/**
 * A holder that manages both SavedStateRegistry and SaveableStateRegistry for navigation destinations.
 * This allows external control over saving and restoring state for all destinations.
 */
public class NavigationSavedStateHolder(
    savedState: SavedState
) {
    private val savedStateRegistryMap = mutableMapOf<String, DestinationSavedStateRegistry>()
    private val saveableStateRegistryMap = mutableMapOf<String, DestinationSaveableStateRegistry>()
    private var savedState by mutableStateOf(savedState)

    /**
     * Gets or creates a [DestinationSavedStateRegistry] for the given destination ID.
     */
    @Composable
    internal fun getSavedStateRegistry(destinationId: String): DestinationSavedStateRegistry {
        return remember(destinationId, savedState) {
            val saved = savedState.read {
                getSavedStateOrNull(destinationId + "_saved")
            }
            savedState.write {
                remove(destinationId + "_saved")
            }
            savedStateRegistryMap.getOrPut(destinationId) {
                DestinationSavedStateRegistry(saved)
            }
        }
    }

    /**
     * Gets or creates a [DestinationSaveableStateRegistry] for the given destination ID.
     *
     * @param canBeSaved A function to determine if a value can be saved
     */
    @Composable
    internal fun getSaveableStateRegistry(
        destinationId: String,
    ): DestinationSaveableStateRegistry {
        val parentSaveableStateRegistry = LocalSaveableStateRegistry.current

        val registry = remember(destinationId, savedState) {
            val saved = savedState.read {
                getSavedStateOrNull(destinationId + "_saveable")?.read {
                    toMap().mapNotNull { (k, v) ->
                        if (v !is List<*>) return@mapNotNull null
                        k to (v as List<Any?>)
                    }.toMap()
                }
            }
            savedState.write {
                remove(destinationId + "_saveable")
            }
            saveableStateRegistryMap.getOrPut(destinationId) {
                DestinationSaveableStateRegistry(
                    restoredValues = saved,
                    canBeSaved = {
                        parentSaveableStateRegistry?.canBeSaved(it) ?: true
                    },
                )
            }
        }
        return registry
    }

    @Composable
    internal fun DestinationDisposedEffect(destinationId: String) {
        val savedStateRegistry = savedStateRegistryMap[destinationId]
        savedStateRegistry?.lifecycle?.currentState = Lifecycle.State.RESUMED
        DisposableEffect(destinationId, savedState) {
            onDispose {
                savedStateRegistry?.lifecycle?.currentState = Lifecycle.State.CREATED
            }
        }
        DisposableEffect(destinationId, savedState) {
            val saveableStateRegistry = saveableStateRegistryMap[destinationId]
            val savedState = savedState
            onDispose {
                if (saveableStateRegistryMap[destinationId] != saveableStateRegistry) return@onDispose
                saveableStateRegistryMap.remove(destinationId)
                if (saveableStateRegistry == null) return@onDispose
                savedState.write {
                    putSavedState(destinationId + "_saveable", savedState(saveableStateRegistry.performSave()))
                }
            }
        }
    }

    /**
     * Saves the state for all destinations.
     *
     * @return A [SavedState] containing all destination states, where each destination ID is a key
     * mapped to another [SavedState] with "saved" and "saveable" entries.
     */
    internal fun saveState(): SavedState {
        return savedState(savedState) {
            // Get all destination IDs from both maps
            val allDestinationIds = (savedStateRegistryMap.keys + saveableStateRegistryMap.keys).toSet()

            allDestinationIds.forEach { destinationId ->
                val savedStateRegistry = savedStateRegistryMap[destinationId]
                val saveableStateRegistry = saveableStateRegistryMap[destinationId]

                // Save the SavedStateRegistry state
                savedStateRegistry?.let {
                    val state = savedState()
                    it.savedStateRegistryController.performSave(state)
                    putSavedState(destinationId+"_saved", state)
                }

                // Save the SaveableStateRegistry state
                saveableStateRegistry?.let {
                    putSavedState(destinationId+"_saveable", savedState(it.performSave()))
                }
            }
        }
    }

    internal fun restoreState(savedState: SavedState) {
        this.savedState = savedState
        savedStateRegistryMap.clear()
        saveableStateRegistryMap.clear()
    }

    /**
     * Removes and cleans up state for a specific destination.
     */
    public fun removeState(destinationId: String) {
        savedStateRegistryMap[destinationId]?.let {
            it.lifecycle.currentState = Lifecycle.State.DESTROYED
        }
        savedStateRegistryMap.remove(destinationId)
        saveableStateRegistryMap.remove(destinationId)
        savedState.write {
            remove(destinationId+"_saved")
            remove(destinationId+"_saveable")
        }
    }

    /**
     * Clears all state.
     */
    public fun clear() {
        savedStateRegistryMap.keys.toList().forEach { removeState(it) }
    }

    internal object Saver : androidx.compose.runtime.saveable.Saver<NavigationSavedStateHolder, SavedState> {
        override fun restore(value: SavedState): NavigationSavedStateHolder? {
            return NavigationSavedStateHolder(value)
        }

        override fun SaverScope.save(value: NavigationSavedStateHolder): SavedState? {
            return value.saveState()
        }
    }
}


