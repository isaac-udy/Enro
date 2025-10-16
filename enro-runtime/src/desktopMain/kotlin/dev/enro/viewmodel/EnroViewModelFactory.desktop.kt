package dev.enro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import dev.enro.NavigationHandle
import kotlin.reflect.KClass


public actual class EnroViewModelFactory actual constructor(
    private val navigationHandle: NavigationHandle<*>,
    private val delegate: ViewModelProvider.Factory,
) : ViewModelProvider.Factory {
    public override fun <T : ViewModel> create(
        modelClass: KClass<T>,
        extras: CreationExtras,
    ): T {
        NavigationHandleProvider.put(modelClass, navigationHandle)
        return delegate.create(modelClass, extras).also {
            NavigationHandleProvider.clear(modelClass)
        }
    }
}