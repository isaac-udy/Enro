package dev.enro.ui.decorators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
 * Stores the `SavedStateRegistry` state and the `SaveableStateRegistry` state of each
 * destination in a container, keyed by destination id.
 */
@Stable
public class NavigationSavedStateHolder @dev.enro.annotations.AdvancedEnroApi constructor(
    savedState: SavedState
) {
    private val savedStateRegistryMap = mutableMapOf<String, DestinationSavedStateRegistry>()
    private val saveableStateRegistryMap = mutableMapOf<String, DestinationSaveableStateRegistry>()
    private var savedState by mutableStateOf(savedState)

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
                // removeState can move the registry to DESTROYED before this onDispose runs,
                // and LifecycleRegistry throws on a move from DESTROYED to CREATED.
                val registry = savedStateRegistry ?: return@onDispose
                if (registry.lifecycle.currentState != Lifecycle.State.DESTROYED) {
                    registry.lifecycle.currentState = Lifecycle.State.CREATED
                }
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
     * Returns a [SavedState] with two entries for each destination: the `SavedStateRegistry`
     * state under the key `<destination id>_saved`, and the `SaveableStateRegistry` state under
     * the key `<destination id>_saveable`.
     */
    internal fun saveState(): SavedState {
        return savedState(savedState) {
            val allDestinationIds = (savedStateRegistryMap.keys + saveableStateRegistryMap.keys).toSet()

            allDestinationIds.forEach { destinationId ->
                val savedStateRegistry = savedStateRegistryMap[destinationId]
                val saveableStateRegistry = saveableStateRegistryMap[destinationId]

                savedStateRegistry?.let {
                    val state = savedState()
                    it.savedStateRegistryController.performSave(state)
                    putSavedState(destinationId+"_saved", state)
                }

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
     * Removes the stored state for [destinationId] and moves the destination's
     * `SavedStateRegistryOwner` lifecycle to `DESTROYED`.
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
     * Calls [removeState] for each destination that has a `SavedStateRegistryOwner`. State that
     * is stored for a destination with no `SavedStateRegistryOwner` is kept.
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


