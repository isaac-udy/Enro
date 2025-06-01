package dev.enro3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import dev.enro3.NavigationHandle
import dev.enro3.NavigationKey
import kotlin.reflect.KClass

@PublishedApi
internal object NavigationHandleProvider {
    private val navigationHandles = mutableMapOf<KClass<*>, NavigationHandle<NavigationKey>>()

    fun put(modelClass: KClass<*>, navigationHandle: NavigationHandle<NavigationKey>) {
        navigationHandles[modelClass] = navigationHandle
    }

    fun clear(modelClass: KClass<*>) {
        navigationHandles.remove(modelClass)
    }

    fun get(modelClass: KClass<*>): NavigationHandle<NavigationKey> {
        return navigationHandles[modelClass]
            ?: error(
                "Could not get a NavigationHandle for ViewModel of type ${modelClass.simpleName}."
            )
    }

    // Called by enro-test
    fun clearAllForTest() {
        navigationHandles.clear()
    }
}

public inline fun <reified T : ViewModel> CreationExtras.createEnroViewModel(noinline block: () -> T): T {
    NavigationHandleProvider.put(T::class, getNavigationHandle())
    val viewModel = block.invoke()
    return viewModel.also {
        NavigationHandleProvider.clear(T::class)
    }
}
