package nav.enro.viewmodel

import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import nav.enro.core.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class ViewModelNavigationHandleProperty<T : NavigationKey> internal constructor(
    viewModelType: KClass<out ViewModel>
) : ReadOnlyProperty<ViewModel, NavigationHandle<T>> {

    private val navigationHandle = EnroViewModelNavigationHandleProvider.get<T>(viewModelType.java)

    override fun getValue(thisRef: ViewModel, property: KProperty<*>): NavigationHandle<T> {
        return navigationHandle
    }
}

fun <T : NavigationKey> ViewModel.navigationHandle(): ViewModelNavigationHandleProperty<T> =
    ViewModelNavigationHandleProperty(this::class)


@MainThread
inline fun <reified VM : ViewModel> FragmentActivity.enroViewModels(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> {

    val factory = factoryProducer ?: {
        defaultViewModelProviderFactory
    }

    val navigationHandle = {
        getNavigationHandle<Nothing>()
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
        getNavigationHandle<Nothing>()
    }

    return enroViewModels({viewModelStore}, navigationHandle, factory)
}

@MainThread
@PublishedApi
internal inline fun <reified VM : ViewModel> enroViewModels(
    noinline viewModelStore: (() -> ViewModelStore),
    noinline navigationHandle: (() -> NavigationHandle<Nothing>),
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
