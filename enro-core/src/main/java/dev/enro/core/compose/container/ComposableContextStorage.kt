package dev.enro.core.compose.container

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.core.NavigationContext
import dev.enro.core.compose.ComposableDestinationContextReference

internal class ComposableContextStorage : ViewModel() {
    val destinations = mutableMapOf<String, MutableMap<String, ComposableDestinationContextReference>>()

    override fun onCleared() {
        destinations.values
            .flatMap { it.values }
            .forEach { it.viewModelStore.clear() }

        super.onCleared()
    }
}

internal fun NavigationContext<*>.getComposableContextStorage(): ComposableContextStorage = ViewModelLazy(
    viewModelClass = ComposableContextStorage::class,
    storeProducer = { viewModelStoreOwner.viewModelStore },
    factoryProducer = { ViewModelProvider.NewInstanceFactory() },
).value