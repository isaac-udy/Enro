package nav.enro.core

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import nav.enro.core.controller.NavigationController

interface NavigationHandle : LifecycleOwner {
    val id: String
    val controller: NavigationController
    val additionalData: Bundle
    fun <T: NavigationKey> key(): T
    fun executeInstruction(navigationInstruction: NavigationInstruction)
}

class TypedNavigationHandle<T : NavigationKey>(private val navigationHandle: NavigationHandle) {
    val id: String get() = navigationHandle.id
    val key: T get() = navigationHandle.key()
    val additionalData: Bundle get() = navigationHandle.additionalData
    val controller: NavigationController get() = navigationHandle.controller

    fun executeInstruction(navigationInstruction: NavigationInstruction) = navigationHandle.executeInstruction(navigationInstruction)
}

fun <T: NavigationKey> NavigationHandle.asTyped(): TypedNavigationHandle<T> {
    return TypedNavigationHandle(this)
}

fun NavigationHandle.forward(key: NavigationKey, vararg childKeys: NavigationKey) =
    executeInstruction(NavigationInstruction.Open(NavigationDirection.FORWARD, key, childKeys.toList()))

fun NavigationHandle.replace(key: NavigationKey, vararg childKeys: NavigationKey) =
    executeInstruction(NavigationInstruction.Open(NavigationDirection.REPLACE, key, childKeys.toList()))

fun NavigationHandle.replaceRoot(key: NavigationKey, vararg childKeys: NavigationKey) =
    executeInstruction(NavigationInstruction.Open(NavigationDirection.REPLACE_ROOT, key, childKeys.toList()))

fun NavigationHandle.close() =
    executeInstruction(NavigationInstruction.Close)

fun TypedNavigationHandle<*>.forward(key: NavigationKey, vararg childKeys: NavigationKey) =
    executeInstruction(NavigationInstruction.Open(NavigationDirection.FORWARD, key, childKeys.toList()))

fun TypedNavigationHandle<*>.replace(key: NavigationKey, vararg childKeys: NavigationKey) =
    executeInstruction(NavigationInstruction.Open(NavigationDirection.REPLACE, key, childKeys.toList()))

fun TypedNavigationHandle<*>.replaceRoot(key: NavigationKey, vararg childKeys: NavigationKey) =
    executeInstruction(NavigationInstruction.Open(NavigationDirection.REPLACE_ROOT, key, childKeys.toList()))

fun TypedNavigationHandle<*>.close() =
    executeInstruction(NavigationInstruction.Close)