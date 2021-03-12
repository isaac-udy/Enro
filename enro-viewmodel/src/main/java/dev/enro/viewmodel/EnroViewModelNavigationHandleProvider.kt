package dev.enro.viewmodel

import dev.enro.core.NavigationHandle

internal object EnroViewModelNavigationHandleProvider {
    private val navigationHandles = mutableMapOf<Class<*>, NavigationHandle>()

    fun put(modelClass: Class<*>, navigationHandle: NavigationHandle) {
        navigationHandles[modelClass] = navigationHandle
    }

    fun clear(modelClass: Class<*>) {
        navigationHandles.remove(modelClass)
    }

    fun get(modelClass: Class<*>): NavigationHandle {
        return navigationHandles[modelClass] as NavigationHandle
    }
}