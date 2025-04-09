package dev.enro.viewmodel

import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import dev.enro.core.getNavigationHandle


@MainThread
public inline fun <reified VM : ViewModel> Fragment.enroViewModels(
    noinline extrasProducer: (() -> CreationExtras)? = null,
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null,
): Lazy<VM> {

    val factory = factoryProducer ?: {
        defaultViewModelProviderFactory
    }

    val navigationHandle = {
        getNavigationHandle()
    }

    return enroViewModels(
        navigationHandle = navigationHandle,
        storeProducer = { viewModelStore },
        factoryProducer = factory,
        extrasProducer = { extrasProducer?.invoke() ?: defaultViewModelCreationExtras }
    )
}