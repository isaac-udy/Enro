package dev.enro3.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.SavedStateViewModelFactory
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
 * Returns a [ViewModelStoreNavEntryDecorator] that is remembered across recompositions.
 *
 * @param [viewModelStoreOwner] The [ViewModelStoreOwner] that provides the [ViewModelStore] to
 *   NavEntries
 * @param [shouldRemoveStoreOwner] A lambda that returns a Boolean for whether the store owner for a
 *   [NavEntry] should be cleared when the [NavEntry] is popped from the backStack. If true, the
 *   entry's ViewModelStoreOwner will be removed.
 */
@Composable
public fun rememberViewModelStoreNavEntryDecorator(
    viewModelStoreOwner: ViewModelStoreOwner =
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        },
    shouldRemoveStoreOwner: () -> Boolean = shouldRemoveViewModelStoreCallback(),
): NavigationDestinationDecorator<out NavigationKey> = remember {
    ViewModelStoreNavEntryDecorator(viewModelStoreOwner.viewModelStore, shouldRemoveStoreOwner)
}

/**
 * Provides the content of a [NavEntry] with a [ViewModelStoreOwner] and provides that
 * [ViewModelStoreOwner] as a [LocalViewModelStoreOwner] so that it is available within the content.
 *
 * This requires the usage of [androidx.navigation3.runtime.SavedStateNavEntryDecorator] to ensure
 * that the [NavEntry] scoped [ViewModel]s can properly provide access to
 * [androidx.lifecycle.SavedStateHandle]s
 *
 * @param [viewModelStore] The [ViewModelStore] that provides to NavEntries
 * @param [shouldRemoveStoreOwner] A lambda that returns a Boolean for whether the store owner for a
 *   [NavEntry] should be cleared when the [NavEntry] is popped from the backStack. If true, the
 *   entry's ViewModelStoreOwner will be removed.
 */
public fun ViewModelStoreNavEntryDecorator(
    viewModelStore: ViewModelStore,
    shouldRemoveStoreOwner: () -> Boolean,
): NavigationDestinationDecorator<out NavigationKey> {
    val storeOwnerProvider: ViewModelStoreStorage = viewModelStore.getViewModelStoreStorage()
    val onPop: (NavigationKey.Instance<NavigationKey>) -> Unit = { instance ->
        if (shouldRemoveStoreOwner()) {
            storeOwnerProvider.clearViewModelStoreOwnerForInstance(instance)
        }
    }
    return navigationDestinationDecorator(onPop) { destination ->
        val viewModelStore = storeOwnerProvider.viewModelStoreForInstance(destination.instance)

        val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
        val childViewModelStoreOwner = remember {
            object :
                ViewModelStoreOwner,
                SavedStateRegistryOwner by savedStateRegistryOwner,
                HasDefaultViewModelProviderFactory {
                override val viewModelStore: ViewModelStore
                    get() = viewModelStore

                override val defaultViewModelProviderFactory: ViewModelProvider.Factory
                    get() = error("defaultViewModelProviderFactory not supported")

                override val defaultViewModelCreationExtras: CreationExtras
                    get() =
                        MutableCreationExtras().also {
                            it[SAVED_STATE_REGISTRY_OWNER_KEY] = savedStateRegistryOwner
                            it[VIEW_MODEL_STORE_OWNER_KEY] = this
                        }

                init {
                    require(this.lifecycle.currentState == Lifecycle.State.INITIALIZED) {
                        "The Lifecycle state is already beyond INITIALIZED. The " +
                                "ViewModelStoreNavEntryDecorator requires adding the " +
                                "SavedStateNavEntryDecorator to ensure support for " +
                                "SavedStateHandles."
                    }
                    enableSavedStateHandles()
                }
            }
        }
        CompositionLocalProvider(LocalViewModelStoreOwner provides childViewModelStoreOwner) {
            destination.Content()
        }
    }
}

private class ViewModelStoreStorage : ViewModel() {
    private val owners = mutableMapOf<String, ViewModelStore>()

    fun viewModelStoreForInstance(instance: NavigationKey.Instance<*>): ViewModelStore {
        return owners.getOrPut(instance.id) { ViewModelStore() }
    }

    fun clearViewModelStoreOwnerForInstance(instance: NavigationKey.Instance<*>) {
        owners.remove(instance.id)?.clear()
    }

    override fun onCleared() {
        owners.forEach { (_, store) -> store.clear() }
    }
}

private fun ViewModelStore.getViewModelStoreStorage(): ViewModelStoreStorage {
    val provider = ViewModelProvider.create(
        store = this,
        factory = viewModelFactory { initializer { ViewModelStoreStorage() } },
    )
    return provider[ViewModelStoreStorage::class]
}


@Composable internal expect fun shouldRemoveViewModelStoreCallback(): () -> Boolean