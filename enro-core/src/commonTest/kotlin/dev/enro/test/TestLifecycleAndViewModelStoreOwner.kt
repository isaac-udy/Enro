package dev.enro.test

import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.InitializerViewModelFactoryBuilder
import androidx.lifecycle.viewmodel.viewModelFactory

class TestLifecycleAndViewModelStoreOwner(
    viewModels: InitializerViewModelFactoryBuilder.() -> Unit = {}
) : LifecycleOwner,
    ViewModelStoreOwner,
    HasDefaultViewModelProviderFactory {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore = ViewModelStore()
    override val defaultViewModelCreationExtras: CreationExtras = CreationExtras.Empty
    override val defaultViewModelProviderFactory: ViewModelProvider.Factory = viewModelFactory(viewModels)

    fun setLifecycleState(state: Lifecycle.State) {
        lifecycleRegistry.currentState = state
    }
}