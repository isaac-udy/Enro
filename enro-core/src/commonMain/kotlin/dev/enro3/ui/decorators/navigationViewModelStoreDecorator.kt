package dev.enro3.ui.decorators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import dev.enro3.NavigationKey

/**
 * Returns a [NavigationDestinationDecorator] that provides [ViewModelStore] functionality
 * to navigation destinations. This decorator ensures that each destination has its own
 * [ViewModelStoreOwner], allowing proper scoping of ViewModels to individual destinations.
 *
 * The decorator also handles cleanup when destinations are removed from the backstack
 * based on the [shouldRemoveStoreOwner] callback.
 *
 * @param viewModelStoreOwner The parent [ViewModelStoreOwner] that provides the [ViewModelStore]
 * @param shouldRemoveStoreOwner A callback that determines if the ViewModelStore should be
 *                               cleared when the destination is removed from the backstack
 */
@Composable
public fun rememberViewModelStoreDecorator(
    viewModelStoreOwner: ViewModelStoreOwner =
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        },
    shouldRemoveStoreOwner: () -> Boolean = rememberShouldRemoveViewModelStoreCallback(),
): NavigationDestinationDecorator<NavigationKey> = remember(viewModelStoreOwner, shouldRemoveStoreOwner) {
    viewModelStoreDecorator(viewModelStoreOwner.viewModelStore, shouldRemoveStoreOwner)
}

/**
 * Creates a [NavigationDestinationDecorator] that provides ViewModelStore functionality.
 *
 * This decorator wraps each destination with its own [ViewModelStoreOwner] and provides
 * that owner as a [LocalViewModelStoreOwner] so that ViewModels can be properly scoped
 * to individual destinations.
 *
 * **Note:** This decorator requires [savedStateDecorator] to be applied before it to ensure
 * that ViewModels can properly provide access to [androidx.lifecycle.SavedStateHandle]s.
 *
 * @param viewModelStore The parent [ViewModelStore] that manages destination-scoped stores
 * @param shouldRemoveStoreOwner A callback that determines if the ViewModelStore should be
 *                               cleared when the destination is removed from the backstack
 */
internal fun viewModelStoreDecorator(
    viewModelStore: ViewModelStore,
    shouldRemoveStoreOwner: () -> Boolean,
): NavigationDestinationDecorator<NavigationKey> {
    val storage = viewModelStore.getOrCreateViewModelStoreStorage()

    return navigationDestinationDecorator(
        onRemove = { instance ->
            if (shouldRemoveStoreOwner()) {
                storage.clearViewModelStoreForInstance(instance)
            }
        },
        decorator = { destination ->
            val destinationViewModelStore = storage.viewModelStoreForInstance(destination.instance)
            val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current

            val childViewModelStoreOwner = remember(savedStateRegistryOwner) {
                DestinationViewModelStoreOwner(
                    destinationViewModelStore = destinationViewModelStore,
                    savedStateRegistryOwner = savedStateRegistryOwner,
                )
            }

            CompositionLocalProvider(LocalViewModelStoreOwner provides childViewModelStoreOwner) {
                destination.Content()
            }
        }
    )
}

/**
 * Internal ViewModelStoreOwner implementation for navigation destinations.
 * Combines ViewModelStore functionality with SavedStateRegistry support.
 */
private class DestinationViewModelStoreOwner(
    private val destinationViewModelStore: ViewModelStore,
    savedStateRegistryOwner: SavedStateRegistryOwner,
) : ViewModelStoreOwner,
    SavedStateRegistryOwner by savedStateRegistryOwner,
    HasDefaultViewModelProviderFactory {

    override val viewModelStore: ViewModelStore
        get() = destinationViewModelStore

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = error("defaultViewModelProviderFactory not supported - use viewModel with explicit factory")

    override val defaultViewModelCreationExtras: CreationExtras
        get() = MutableCreationExtras().also {
            it[SAVED_STATE_REGISTRY_OWNER_KEY] = this
            it[VIEW_MODEL_STORE_OWNER_KEY] = this
        }

    init {
        require(lifecycle.currentState == Lifecycle.State.INITIALIZED) {
            "The Lifecycle state is already beyond INITIALIZED. The " +
                    "ViewModelStoreDecorator requires adding the " +
                    "SavedStateDecorator to ensure support for " +
                    "SavedStateHandles."
        }
        enableSavedStateHandles()
    }
}

/**
 * Internal storage for managing ViewModelStores per navigation instance.
 * This ViewModel is stored in the parent ViewModelStore and manages child stores.
 */
private class ViewModelStoreStorage : ViewModel() {
    private val stores = mutableMapOf<String, ViewModelStore>()

    fun viewModelStoreForInstance(instance: NavigationKey.Instance<*>): ViewModelStore {
        return stores.getOrPut(instance.id) { ViewModelStore() }
    }

    fun clearViewModelStoreForInstance(instance: NavigationKey.Instance<*>) {
        stores.remove(instance.id)?.clear()
    }

    override fun onCleared() {
        stores.forEach { (_, store) -> store.clear() }
        stores.clear()
    }
}

/**
 * Gets or creates the ViewModelStoreStorage from the parent ViewModelStore.
 */
private fun ViewModelStore.getOrCreateViewModelStoreStorage(): ViewModelStoreStorage {
    val provider = ViewModelProvider.create(
        store = this,
        factory = viewModelFactory {
            initializer { ViewModelStoreStorage() }
        },
    )
    return provider[ViewModelStoreStorage::class]
}

/**
 * Platform-specific callback for determining when to remove ViewModelStores.
 * This is typically based on whether the navigation is temporary (e.g., configuration change)
 * or permanent (e.g., back navigation).
 */
@Composable
internal expect fun rememberShouldRemoveViewModelStoreCallback(): () -> Boolean
