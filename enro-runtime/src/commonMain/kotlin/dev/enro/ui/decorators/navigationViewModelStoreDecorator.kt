package dev.enro.ui.decorators

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
import dev.enro.NavigationKey
import dev.enro.handle.NavigationHandleHolder
import dev.enro.ui.LocalNavigationContext

/**
 * Returns a decorator that provides each destination with a separate [ViewModelStoreOwner]
 * through [LocalViewModelStoreOwner].
 *
 * Apply [rememberSavedStateDecorator] before this decorator: the destination's
 * [ViewModelStoreOwner] enables `SavedStateHandle` support on the [SavedStateRegistryOwner]
 * that [rememberSavedStateDecorator] provides.
 *
 * @param viewModelStoreOwner The owner of the [ViewModelStore] that stores each destination's
 *   [ViewModelStore]. Throws when omitted and [LocalViewModelStoreOwner] provides no owner.
 * @param shouldRemoveStoreOwner Called from the decorator's `onPop`. The destination's
 *   [ViewModelStore] is cleared when [shouldRemoveStoreOwner] returns `true`. The default
 *   returns `false` on Android while the Activity is changing configurations, and `true`
 *   otherwise.
 */
@Composable
public fun rememberViewModelStoreDecorator(
    viewModelStoreOwner: ViewModelStoreOwner =
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        },
    shouldRemoveStoreOwner: () -> Boolean = rememberShouldRemoveViewModelStoreCallback(),
): NavigationDestinationDecorator<NavigationKey> {
    val contextViewModelStoreOwner = LocalNavigationContext.current
    return remember(viewModelStoreOwner, shouldRemoveStoreOwner) {
        viewModelStoreDecorator(
            parentViewModelStoreOwner = contextViewModelStoreOwner,
            viewModelStore = viewModelStoreOwner.viewModelStore,
            shouldRemoveStoreOwner = shouldRemoveStoreOwner
        )
    }
}

/** Requires [savedStateDecorator] earlier in the decorator list. */
internal fun viewModelStoreDecorator(
    parentViewModelStoreOwner: ViewModelStoreOwner,
    viewModelStore: ViewModelStore,
    shouldRemoveStoreOwner: () -> Boolean,
): NavigationDestinationDecorator<NavigationKey> {
    val storage = viewModelStore.getOrCreateViewModelStoreStorage()

    return navigationDestinationDecorator(
        onPop = { instance ->
            if (shouldRemoveStoreOwner()) {
                storage.clearViewModelStoreForInstance(instance)
            }
        },
        decorate = { destination ->
            val destinationViewModelStore = storage.viewModelStoreForInstance(destination.instance)
            val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current

            val childViewModelStoreOwner = remember(savedStateRegistryOwner) {
                DestinationViewModelStoreOwner(
                    parentViewModelStoreOwner = parentViewModelStoreOwner,
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

private class DestinationViewModelStoreOwner(
    private val parentViewModelStoreOwner: ViewModelStoreOwner,
    private val destinationViewModelStore: ViewModelStore,
    savedStateRegistryOwner: SavedStateRegistryOwner,
) : ViewModelStoreOwner,
    SavedStateRegistryOwner by savedStateRegistryOwner,
    HasDefaultViewModelProviderFactory {

    override val viewModelStore: ViewModelStore
        get() = destinationViewModelStore

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() {
            when (parentViewModelStoreOwner) {
                is HasDefaultViewModelProviderFactory -> {
                    return parentViewModelStoreOwner.defaultViewModelProviderFactory
                }
                else -> {
                    error("defaultViewModelProviderFactory not supported - use viewModel with explicit factory")
                }
            }
        }

    override val defaultViewModelCreationExtras: CreationExtras
        get() = MutableCreationExtras().also {
            it[SAVED_STATE_REGISTRY_OWNER_KEY] = this
            it[VIEW_MODEL_STORE_OWNER_KEY] = this
        }

    init {
        // enableSavedStateHandles() must run once per registry; see
        // DestinationSavedStateRegistry.savedStateHandlesEnabled. A registry of any other type
        // always takes the lifecycle check, which fails when savedStateDecorator is missing.
        val destinationRegistry = savedStateRegistryOwner as? DestinationSavedStateRegistry
        if (destinationRegistry == null || !destinationRegistry.savedStateHandlesEnabled) {
            require(
                lifecycle.currentState == Lifecycle.State.INITIALIZED ||
                    lifecycle.currentState == Lifecycle.State.CREATED
            ) {
                "The Lifecycle state is already beyond CREATED. The " +
                    "ViewModelStoreDecorator requires adding the " +
                    "SavedStateDecorator to ensure support for " +
                    "SavedStateHandles."
            }
            enableSavedStateHandles()
            destinationRegistry?.savedStateHandlesEnabled = true
        }
    }
}

/**
 * Stored in the parent ViewModelStore, so that clearing the parent ViewModelStore clears each
 * destination's ViewModelStore through [onCleared].
 */
private class ViewModelStoreStorage : ViewModel() {
    private val stores = mutableMapOf<String, StoreEntry>()

    private class StoreEntry(
        val instance: NavigationKey.Instance<*>,
        val store: ViewModelStore,
    )

    fun viewModelStoreForInstance(instance: NavigationKey.Instance<*>): ViewModelStore {
        return stores.getOrPut(instance.id) { StoreEntry(instance, ViewModelStore()) }.store
    }

    fun clearViewModelStoreForInstance(instance: NavigationKey.Instance<*>) {
        val entry = stores.remove(instance.id) ?: return
        entry.store.clearAndReseedNavigationHandle(instance)
    }

    override fun onCleared() {
        stores.forEach { (_, entry) ->
            entry.store.clearAndReseedNavigationHandle(entry.instance)
        }
        stores.clear()
    }
}

/**
 * Clears the store, then adds a cleared NavigationHandleHolder to the store.
 *
 * A destination can create a ViewModel in the store after the store is cleared: the content of
 * a Dialog (or of a ModalBottomSheet on iOS) is a separate composition that is disposed
 * asynchronously. With the cleared holder, a ViewModel created after the clear receives a
 * NavigationHandle that ignores operations, and the lookup in getNavigationHandleHolder and
 * `navigationHandle()` does not throw.
 */
private fun ViewModelStore.clearAndReseedNavigationHandle(instance: NavigationKey.Instance<*>) {
    clear()
    ViewModelProvider.create(
        store = this,
        factory = viewModelFactory {
            initializer { NavigationHandleHolder.cleared(instance) }
        },
    )[NavigationHandleHolder::class]
}

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
 * The returned callback returns `false` on Android while the Activity is changing
 * configurations, and `true` otherwise and on every other platform.
 */
@Composable
internal expect fun rememberShouldRemoveViewModelStoreCallback(): () -> Boolean
