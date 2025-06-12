package dev.enro.ui.decorators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedState
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import dev.enro.NavigationKey

/**
 * Returns a [NavigationDestinationDecorator] that provides saved state functionality to navigation destinations.
 * This decorator wraps each destination with proper state management to ensure that calls to [rememberSaveable]
 * within the destination content work properly and that state can be saved.
 *
 * It also provides the destination content with a [SavedStateRegistryOwner] which can be accessed
 * via [LocalSavedStateRegistryOwner].
 *
 * This decorator is **required** for proper state preservation across configuration changes
 * and process death.
 *
 * @param navigationSavedStateHolder The [NavigationSavedStateHolder] that manages the saved state for destinations
 */
@Composable
public fun rememberSavedStateDecorator(
    navigationSavedStateHolder: NavigationSavedStateHolder,
): NavigationDestinationDecorator<NavigationKey> = remember(navigationSavedStateHolder) {
    savedStateDecorator(
        navigationSavedStateHolder,
    )
}

/**
 * Creates a [NavigationDestinationDecorator] that provides saved state functionality.
 *
 * @param navigationSavedStateHolder The [NavigationSavedStateHolder] that manages the saved state for destinations
 */
internal fun savedStateDecorator(
    navigationSavedStateHolder: NavigationSavedStateHolder,
): NavigationDestinationDecorator<NavigationKey> {
    return navigationDestinationDecorator<NavigationKey>(
        onRemove = { instance ->
            val id = instance.id
            navigationSavedStateHolder.removeState(id)
        },
        decorator = { destination ->
            val instance = destination.instance
            val id = instance.id

            val childRegistry = navigationSavedStateHolder.getSavedStateRegistry(id)
            val saveableRegistry = navigationSavedStateHolder.getSaveableStateRegistry(id)
            CompositionLocalProvider(
                LocalSavedStateRegistryOwner provides childRegistry,
                LocalSaveableStateRegistry provides saveableRegistry.saveableStateRegistry
            ) {
                destination.content()
            }
            navigationSavedStateHolder.DestinationDisposedEffect(id)
        }
    )
}

/**
 * Internal implementation of [SavedStateRegistryOwner] for navigation destinations.
 * Manages the lifecycle and saved state registry for a single destination.
 */
internal class DestinationSavedStateRegistry(
    savedState: SavedState?,
) : SavedStateRegistryOwner {
    override val lifecycle: LifecycleRegistry = LifecycleRegistry(this)

    val savedStateRegistryController: SavedStateRegistryController =
        SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry =
        savedStateRegistryController.savedStateRegistry

    init {
        savedStateRegistryController.performRestore(savedState)
    }
}

/**
 * Internal implementation of [SaveableStateRegistry] wrapper for navigation destinations.
 * Manages the saveable state registry for a single destination.
 */
internal class DestinationSaveableStateRegistry(
    private var restoredValues: Map<String, List<Any?>>?,
) {

    val saveableStateRegistry: SaveableStateRegistry by lazy {
        SaveableStateRegistry(
            restoredValues = restoredValues
        ) {
            // TODO we currently save all things, because we need to do this for savedState and @Serializable,
            //  but it would be really good if we could tell if something was @Serializable, and possibly
            //  delegate the "can save" to a parent
            true
        }
    }

    fun performSave(): Map<String, List<Any?>> {
        return saveableStateRegistry.performSave()
    }
}

