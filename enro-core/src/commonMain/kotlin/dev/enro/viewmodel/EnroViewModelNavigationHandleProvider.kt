package dev.enro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import dev.enro.core.EnroException
import dev.enro.core.NavigationHandle
import kotlin.reflect.KClass

internal object EnroViewModelNavigationHandleProvider {
    private val navigationHandles = mutableMapOf<KClass<*>, NavigationHandle>()

    fun put(modelClass: KClass<*>, navigationHandle: NavigationHandle) {
        navigationHandles[modelClass] = navigationHandle
    }

    fun clear(modelClass: KClass<*>) {
        navigationHandles.remove(modelClass)
    }

    fun get(modelClass: KClass<*>): NavigationHandle {
        return navigationHandles[modelClass]
            ?: throw EnroException.ViewModelCouldNotGetNavigationHandle(
                "Could not get a NavigationHandle inside of ViewModel of type ${modelClass.simpleName}. Make sure you are using `by enroViewModels` and not `by viewModels`."
            )
    }

    // Called by enro-test
    fun clearAllForTest() {
        navigationHandles.clear()
    }
}

public inline fun <reified T : ViewModel> CreationExtras.createEnroViewModel(noinline block: () -> T): T {
    return createEnroViewModel(T::class, block)
}

public fun <T : ViewModel> CreationExtras.createEnroViewModel(
    viewModelType: KClass<T>,
    block: () -> T,
): T {
    EnroViewModelNavigationHandleProvider.put(viewModelType, getNavigationHandle())
    val viewModel = block.invoke()
    return viewModel.also {
        EnroViewModelNavigationHandleProvider.clear(viewModelType)
    }
}