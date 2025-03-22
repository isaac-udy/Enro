package dev.enro.viewmodel

import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.getNavigationHandleTag
import androidx.lifecycle.viewmodel.CreationExtras
import dev.enro.core.LazyNavigationHandleConfiguration
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.TypedNavigationHandle
import dev.enro.core.asTyped
import dev.enro.core.getNavigationHandle
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

public class ViewModelNavigationHandleProperty<T : NavigationKey> @PublishedApi internal constructor(
    viewModelType: KClass<out ViewModel>,
    type: KClass<T>,
    block: LazyNavigationHandleConfiguration<T>.() -> Unit
) : ReadOnlyProperty<ViewModel, TypedNavigationHandle<T>> {

    private val navigationHandle = EnroViewModelNavigationHandleProvider.get(viewModelType)
        .asTyped(type)
        .apply {
            LazyNavigationHandleConfiguration(type)
                .apply(block)
                .configure(this)
        }

    override fun getValue(thisRef: ViewModel, property: KProperty<*>): TypedNavigationHandle<T> {
        return navigationHandle
    }
}

public fun <T : NavigationKey> ViewModel.navigationHandle(
    type: KClass<T>,
    block: LazyNavigationHandleConfiguration<T>.() -> Unit = {}
): ViewModelNavigationHandleProperty<T> =
    ViewModelNavigationHandleProperty(this::class, type, block)

public inline fun <reified T : NavigationKey> ViewModel.navigationHandle(
    noinline block: LazyNavigationHandleConfiguration<T>.() -> Unit = {}
): ViewModelNavigationHandleProperty<T> = navigationHandle(T::class, block)

@PublishedApi
internal fun ViewModel.getNavigationHandle(): NavigationHandle {
    return getNavigationHandleTag() ?: EnroViewModelNavigationHandleProvider.get(this::class)
}

@MainThread
public inline fun <reified VM : ViewModel> ComponentActivity.enroViewModels(
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
