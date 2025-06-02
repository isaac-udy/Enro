package dev.enro3.ui.decorators

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
 * Returns a [NavigationDestinationDecorator] that provides saved state functionality to navigation destinations.
 * This decorator wraps each destination with a [SaveableStateHolder.SaveableStateProvider] to ensure
 * that calls to [rememberSaveable] within the destination content work properly and that state can be saved.
 *
 * It also provides the destination content with a [SavedStateRegistryOwner] which can be accessed
 * via [LocalSavedStateRegistryOwner].
 *
 * This decorator is **required** for proper state preservation across configuration changes
 * and process death.
 *
 * @param saveableStateHolder The [SaveableStateHolder] that manages the saved state for destinations
 */
@Composable
public fun rememberSavedStateDecorator(
    saveableStateHolder: SaveableStateHolder = rememberSaveableStateHolder(),
): NavigationDestinationDecorator<NavigationKey> = remember(saveableStateHolder) {
    savedStateDecorator(saveableStateHolder)
}

/**
 * Creates a [NavigationDestinationDecorator] that provides saved state functionality.
 *
 * @param saveableStateHolder The [SaveableStateHolder] that manages the saved state for destinations
 */
internal fun savedStateDecorator(
    saveableStateHolder: SaveableStateHolder,
): NavigationDestinationDecorator<NavigationKey> {
    val registryMap = mutableMapOf<String, DestinationSavedStateRegistry>()

    return navigationDestinationDecorator<NavigationKey>(
        onRemove = { instance ->
            val id = instance.id
            if (registryMap.contains(id)) {
                // Remove state from saveableStateHolder
                saveableStateHolder.removeState(id)

                // Save and clean up saved state registry
                val savedState = savedState()
                val childRegistry = registryMap.getValue(id)
                childRegistry.savedStateRegistryController.performSave(savedState)
                childRegistry.savedState = savedState
                childRegistry.lifecycle.currentState = Lifecycle.State.DESTROYED
            }
        },
        decorator = { destination ->
            val instance = destination.instance
            val id = instance.id

            val childRegistry by rememberSaveable(
                id,
                stateSaver = DestinationSavedStateRegistry.Saver
            ) {
                mutableStateOf(DestinationSavedStateRegistry())
            }
            registryMap[id] = childRegistry

            saveableStateHolder.SaveableStateProvider(id) {
                CompositionLocalProvider(LocalSavedStateRegistryOwner provides childRegistry) {
                    destination.content()
                }
            }
            childRegistry.lifecycle.currentState = Lifecycle.State.RESUMED
        }
    )
}

/**
 * Internal implementation of [SavedStateRegistryOwner] for navigation destinations.
 * Manages the lifecycle and saved state registry for a single destination.
 */
internal class DestinationSavedStateRegistry : SavedStateRegistryOwner {
    override val lifecycle: LifecycleRegistry = LifecycleRegistry(this)

    val savedStateRegistryController: SavedStateRegistryController =
        SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry =
        savedStateRegistryController.savedStateRegistry

    var savedState: SavedState? = null

    init {
        savedStateRegistryController.performRestore(savedState)
    }

    companion object {
        val Saver = Saver<DestinationSavedStateRegistry, SavedState>(
            save = { it.savedState },
            restore = { savedState ->
                DestinationSavedStateRegistry().apply {
                    this.savedState = savedState
                }
            }
        )
    }
}
