package dev.enro.core.compose.container

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import dev.enro.core.NavigationContext
import dev.enro.core.compose.destination.ComposableDestinationOwner

internal class ComposableDestinationOwnerStorage : ViewModel() {
    val destinations = mutableMapOf<String, MutableMap<String, ComposableDestinationOwner>>()

    override fun onCleared() {
        destinations.values
            .flatMap { it.values }
            .forEach { it.clear() }

        super.onCleared()
    }
}

internal fun NavigationContext<*>.getComposableContextStorage(): ComposableDestinationOwnerStorage = ViewModelLazy(
    viewModelClass = ComposableDestinationOwnerStorage::class,
    storeProducer = { viewModelStoreOwner.viewModelStore },
    factoryProducer = { ViewModelProvider.NewInstanceFactory() },
).value