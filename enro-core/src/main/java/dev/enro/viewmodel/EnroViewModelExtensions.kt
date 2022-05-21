package dev.enro.viewmodel

import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.getNavigationHandleTag
import dev.enro.core.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class ViewModelNavigationHandleProperty<T : NavigationKey> @PublishedApi internal constructor(
    viewModelType: KClass<out ViewModel>,
    type: KClass<T>,
    block: LazyNavigationHandleConfiguration<T>.() -> Unit
) : ReadOnlyProperty<ViewModel, TypedNavigationHandle<T>> {

    private val navigationHandle = EnroViewModelNavigationHandleProvider.get(viewModelType.java)
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

fun <T : NavigationKey> ViewModel.navigationHandle(
    type: KClass<T>,
    block: LazyNavigationHandleConfiguration<T>.() -> Unit = {}
): ViewModelNavigationHandleProperty<T> =
    ViewModelNavigationHandleProperty(this::class, type, block)

inline fun <reified T : NavigationKey> ViewModel.navigationHandle(
    noinline block: LazyNavigationHandleConfiguration<T>.() -> Unit = {}
): ViewModelNavigationHandleProperty<T> = navigationHandle(T::class, block)

@PublishedApi
internal fun ViewModel.getNavigationHandle(): NavigationHandle {
    return getNavigationHandleTag() ?: EnroViewModelNavigationHandleProvider.get(this::class.java)
}

@MainThread
inline fun <reified VM : ViewModel> FragmentActivity.enroViewModels(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> {

    val factory = factoryProducer ?: {
        defaultViewModelProviderFactory
    }

    val navigationHandle = {
        getNavigationHandle()
    }

    return enroViewModels({viewModelStore}, navigationHandle, factory)
}

@MainThread
inline fun <reified VM : ViewModel> Fragment.enroViewModels(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> {

    val factory = factoryProducer ?: {
        defaultViewModelProviderFactory
    }

    val navigationHandle = {
        getNavigationHandle()
    }

    return enroViewModels({viewModelStore}, navigationHandle, factory)
}

@MainThread
@PublishedApi
internal inline fun <reified VM : ViewModel> enroViewModels(
    noinline viewModelStore: (() -> ViewModelStore),
    noinline navigationHandle: (() -> NavigationHandle),
    noinline factoryProducer: (() -> ViewModelProvider.Factory)
): Lazy<VM> {

    return lazy {
        val factory = EnroViewModelFactory(
            navigationHandle.invoke(),
            factoryProducer.invoke()
        )
        ViewModelProvider(viewModelStore.invoke(), factory)
            .get(VM::class.java)
    }
}
