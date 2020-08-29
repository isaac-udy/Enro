package nav.enro.viewmodel

import nav.enro.core.NavigationHandle
import nav.enro.core.NavigationKey
import kotlin.reflect.KClass

internal object EnroViewModelNavigationHandleProvider {
    private val navigationHandles = mutableMapOf<Class<*>, NavigationHandle<*>>()

    fun put(modelClass: Class<*>, navigationHandle: NavigationHandle<*>) {
        navigationHandles[modelClass] = navigationHandle
    }

    fun clear(modelClass: Class<*>) {
        navigationHandles.remove(modelClass)
    }

    fun <T : NavigationKey> get(modelClass: Class<*>): NavigationHandle<T> {
        return navigationHandles[modelClass] as NavigationHandle<T>
    }
}