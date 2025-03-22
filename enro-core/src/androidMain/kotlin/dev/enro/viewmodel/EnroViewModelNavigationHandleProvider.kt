package dev.enro.viewmodel

import androidx.annotation.Keep
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
    @Keep
    fun clearAllForTest() {
        navigationHandles.clear()
    }
}