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
 * Returns a decorator that provides each destination with a [SavedStateRegistryOwner] through
 * [LocalSavedStateRegistryOwner] and a [SaveableStateRegistry] through
 * [LocalSaveableStateRegistry]. [rememberSaveable] in a destination stores state in
 * [navigationSavedStateHolder].
 *
 * The decorator's `onPop` removes the destination's state from [navigationSavedStateHolder].
 */
@Composable
public fun rememberSavedStateDecorator(
    navigationSavedStateHolder: NavigationSavedStateHolder,
): NavigationDestinationDecorator<NavigationKey> = remember(navigationSavedStateHolder) {
    savedStateDecorator(
        navigationSavedStateHolder,
    )
}

internal fun savedStateDecorator(
    navigationSavedStateHolder: NavigationSavedStateHolder,
): NavigationDestinationDecorator<NavigationKey> {
    return navigationDestinationDecorator<NavigationKey>(
        onPop = { instance ->
            val id = instance.id
            navigationSavedStateHolder.removeState(id)
        },
        decorate = { destination ->
            val instance = destination.instance
            val id = instance.id

            val childRegistry = navigationSavedStateHolder.getSavedStateRegistry(id)
            val saveableRegistry = navigationSavedStateHolder.getSaveableStateRegistry(id)
            CompositionLocalProvider(
                LocalSavedStateRegistryOwner provides childRegistry,
                LocalSaveableStateRegistry provides saveableRegistry.saveableStateRegistry
            ) {
                destination.Content()
            }
            navigationSavedStateHolder.DestinationDisposedEffect(id)
        }
    )
}

internal class DestinationSavedStateRegistry(
    savedState: SavedState?,
) : SavedStateRegistryOwner {
    override val lifecycle: LifecycleRegistry = LifecycleRegistry(this)

    val savedStateRegistryController: SavedStateRegistryController =
        SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry =
        savedStateRegistryController.savedStateRegistry

    /**
     * `true` once `enableSavedStateHandles()` has run for this registry.
     * `enableSavedStateHandles()` registers an observer on every call, and Compose can create a
     * second `DestinationViewModelStoreOwner` for the same registry when movable content is
     * inserted at a new slot, so the second owner must skip the call.
     */
    var savedStateHandlesEnabled: Boolean = false

    init {
        savedStateRegistryController.performRestore(savedState)
    }
}

internal class DestinationSaveableStateRegistry(
    private var restoredValues: Map<String, List<Any?>>?,
) {

    val saveableStateRegistry: SaveableStateRegistry by lazy {
        SaveableStateRegistry(
            restoredValues = restoredValues
        ) {
            // TODO Every value is accepted, because SavedState and @Serializable values must be
            //  saved. Detect @Serializable values, or delegate canBeSaved to a parent registry.
            true
        }
    }

    fun performSave(): Map<String, List<Any?>> {
        return saveableStateRegistry.performSave()
    }
}

