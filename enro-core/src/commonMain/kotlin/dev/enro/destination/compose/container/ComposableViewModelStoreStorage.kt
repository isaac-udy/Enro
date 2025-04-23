package dev.enro.core.compose.container

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext

internal class ComposableViewModelStoreStorage : ViewModel() {
    private val viewModelStores = mutableMapOf<NavigationContainerKey, MutableMap<String, ViewModelStore>>()

    fun getStorageForContainer(key: NavigationContainerKey): MutableMap<String, ViewModelStore> {
        return viewModelStores.getOrPut(key) { mutableMapOf() }
    }

    fun clearStorageForContainer(key: NavigationContainerKey) {
        viewModelStores[key]?.values?.forEach(ViewModelStore::clear)
        viewModelStores.remove(key)
    }

    override fun onCleared() {
        viewModelStores.values
            .flatMap { it.values }
            .forEach { it.clear() }

        super.onCleared()
    }
}

internal fun NavigationContext<*>.getComposableViewModelStoreStorage(): ComposableViewModelStoreStorage = ViewModelLazy(
    viewModelClass = ComposableViewModelStoreStorage::class,
    storeProducer = { viewModelStoreOwner.viewModelStore },
    factoryProducer = {
        viewModelFactory {
            addInitializer(ComposableViewModelStoreStorage::class) {
                ComposableViewModelStoreStorage()
            }
        }
    },
).value