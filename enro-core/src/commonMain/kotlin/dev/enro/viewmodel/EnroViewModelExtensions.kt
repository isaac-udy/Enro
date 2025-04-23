package dev.enro.viewmodel

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.CreationExtras
import dev.enro.core.LazyNavigationHandleConfiguration
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.TypedNavigationHandle
import dev.enro.core.asTyped
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

public class ViewModelNavigationHandleProperty<T : NavigationKey> @PublishedApi internal constructor(
    viewModel: ViewModel,
    type: KClass<T>,
    block: LazyNavigationHandleConfiguration<T>.() -> Unit
) : ReadOnlyProperty<ViewModel, TypedNavigationHandle<T>> {

    private val navigationHandle = run {
        EnroViewModelNavigationHandleProvider.get(viewModel::class)
            .asTyped(type)
            .apply {
                LazyNavigationHandleConfiguration(type)
                    .apply(block)
                    .configure(this)
            }
    }

    override fun getValue(thisRef: ViewModel, property: KProperty<*>): TypedNavigationHandle<T> {
        return navigationHandle
    }
}

public fun <T : NavigationKey> ViewModel.navigationHandle(
    type: KClass<T>,
    block: LazyNavigationHandleConfiguration<T>.() -> Unit = {}
): ViewModelNavigationHandleProperty<T> =
    ViewModelNavigationHandleProperty(this, type, block)

public inline fun <reified T : NavigationKey> ViewModel.navigationHandle(
    noinline block: LazyNavigationHandleConfiguration<T>.() -> Unit = {}
): ViewModelNavigationHandleProperty<T> = navigationHandle(T::class, block)

@PublishedApi
internal fun ViewModel.getNavigationHandle(): NavigationHandle {
    return navigationHandle ?: EnroViewModelNavigationHandleProvider.get(this::class)
}


@MainThread
@PublishedApi
internal inline fun <reified VM : ViewModel> enroViewModels(
    noinline navigationHandle: (() -> NavigationHandle),
    noinline storeProducer: (() -> ViewModelStore),
    noinline factoryProducer: (() -> ViewModelProvider.Factory),
    noinline extrasProducer: () -> CreationExtras = { CreationExtras.Empty }
): Lazy<VM> {
    return ViewModelLazy(
        VM::class,
        storeProducer,
        {
            EnroViewModelFactory(
                navigationHandle.invoke(),
                factoryProducer.invoke()
            )
        },
        extrasProducer,
    )
}
