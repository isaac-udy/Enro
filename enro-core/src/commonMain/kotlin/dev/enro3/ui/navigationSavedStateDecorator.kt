package dev.enro3.ui


import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedState
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import androidx.savedstate.savedState
import dev.enro3.NavigationKey

/**
 * Returns a [SavedStateNavEntryDecorator] that is remembered across recompositions.
 *
 * @param saveableStateHolder the [SaveableStateHolder] that scopes the returned NavEntryDecorator
 */
@Composable
public fun rememberSavedStateDecorator(
    saveableStateHolder: SaveableStateHolder = rememberSaveableStateHolder()
): NavigationDestinationDecorator<out NavigationKey> = remember { SavedStateNavEntryDecorator(saveableStateHolder) }

/**
 * Wraps the content of a [NavEntry] with a [SaveableStateHolder.SaveableStateProvider] to ensure
 * that calls to [rememberSaveable] within the content work properly and that state can be saved.
 * Also provides the content of a [NavEntry] with a [SavedStateRegistryOwner] which can be accessed
 * in the content with [LocalSavedStateRegistryOwner].
 *
 * This [NavEntryDecorator] is the only one that is **required** as saving state is considered a
 * non-optional feature.
 */
public fun SavedStateNavEntryDecorator(
    saveableStateHolder: SaveableStateHolder
): NavigationDestinationDecorator<out NavigationKey> {
    val registryMap = mutableMapOf<String, EntrySavedStateRegistry>()

    val onPop: (NavigationKey.Instance<out NavigationKey>) -> Unit = { instance ->
        val id = instance.id
        if (registryMap.contains(id)) {
            // saveableStateHolder onPop
            saveableStateHolder.removeState(id)

            // saved state onPop
            val savedState = savedState()
            val childRegistry = registryMap.getValue(id)
            childRegistry.savedStateRegistryController.performSave(savedState)
            childRegistry.savedState = savedState
            childRegistry.lifecycle.currentState = Lifecycle.State.DESTROYED
        }
    }

    return navigationDestinationDecorator(onPop = onPop) { destination ->
        val instance = destination.instance
        val id = instance.id

        val childRegistry by
        rememberSaveable(
            id,
            stateSaver =
                Saver(
                    save = { it.savedState },
                    restore = { EntrySavedStateRegistry().apply { savedState = it } },
                ),
        ) {
            mutableStateOf(EntrySavedStateRegistry())
        }
        registryMap.put(id, childRegistry)

        saveableStateHolder.SaveableStateProvider(id) {
            CompositionLocalProvider(LocalSavedStateRegistryOwner provides childRegistry) {
                destination.Content()
            }
        }
        childRegistry.lifecycle.currentState = Lifecycle.State.RESUMED
    }
}

internal class EntrySavedStateRegistry : SavedStateRegistryOwner {
    override val lifecycle: LifecycleRegistry = LifecycleRegistry(this)
    val savedStateRegistryController: SavedStateRegistryController =
        SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry =
        savedStateRegistryController.savedStateRegistry

    var savedState: SavedState? = null

    init {
        savedStateRegistryController.performRestore(savedState)
    }
}