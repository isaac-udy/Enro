package dev.enro.core.compose.container

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext

internal class ComposableViewModelStoreStorage : ViewModel() {
    val viewModelStores = mutableMapOf<NavigationContainerKey, MutableMap<String, ViewModelStore>>()

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
    factoryProducer = { ViewModelProvider.NewInstanceFactory() },
).value