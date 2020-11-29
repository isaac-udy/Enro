package nav.enro.viewmodel

import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import nav.enro.core.NavigationHandle
import nav.enro.core.getNavigationHandle
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class ViewModelNavigationHandleProperty internal constructor(
    viewModelType: KClass<out ViewModel>
) : ReadOnlyProperty<ViewModel, NavigationHandle> {

    private val navigationHandle = EnroViewModelNavigationHandleProvider.get(viewModelType.java)

    override fun getValue(thisRef: ViewModel, property: KProperty<*>): NavigationHandle {
        return navigationHandle
    }
}

fun ViewModel.navigationHandle(): ViewModelNavigationHandleProperty =
    ViewModelNavigationHandleProperty(this::class)


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
