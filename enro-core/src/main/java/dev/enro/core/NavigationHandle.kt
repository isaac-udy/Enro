package dev.enro.core

import android.os.Bundle
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import dev.enro.core.controller.NavigationController
import kotlin.reflect.KClass

interface NavigationHandle : LifecycleOwner {
    val id: String
    val controller: NavigationController
    val additionalData: Bundle
    val key: NavigationKey
    fun executeInstruction(navigationInstruction: NavigationInstruction)
}

interface TypedNavigationHandle<T: NavigationKey> : NavigationHandle {
    override val id: String
    override val controller: NavigationController
    override val additionalData: Bundle
    override val key: T
    override fun executeInstruction(navigationInstruction: NavigationInstruction)
}

@PublishedApi
internal class TypedNavigationHandleImpl<T : NavigationKey>(
    internal val navigationHandle: NavigationHandle,
    private val type: Class<T>
): TypedNavigationHandle<T> {
    override val id: String get() = navigationHandle.id
    override val controller: NavigationController get() = navigationHandle.controller
    override val additionalData: Bundle get() = navigationHandle.additionalData

    @Suppress("UNCHECKED_CAST")
    override val key: T get() = navigationHandle.key as? T
        ?: throw EnroException.IncorrectlyTypedNavigationHandle("TypedNavigationHandle failed to cast key of type ${navigationHandle.key::class.java.simpleName} to ${type.simpleName}")

    override fun getLifecycle(): Lifecycle = navigationHandle.lifecycle

    override fun executeInstruction(navigationInstruction: NavigationInstruction) = navigationHandle.executeInstruction(navigationInstruction)
}

fun <T: NavigationKey> NavigationHandle.asTyped(type: KClass<T>): TypedNavigationHandle<T> {
    val keyType = key::class
    val isValidType = type.java.isAssignableFrom(keyType.java)
    if(!isValidType) {
        throw EnroException.IncorrectlyTypedNavigationHandle("Failed to cast NavigationHandle with key of type ${keyType.java.simpleName} to TypedNavigationHandle<${type.simpleName}>")
    }
    return TypedNavigationHandleImpl(this, type.java)
}

inline fun <reified T: NavigationKey> NavigationHandle.asTyped(): TypedNavigationHandle<T> {
    if(key !is T) {
        throw EnroException.IncorrectlyTypedNavigationHandle("Failed to cast NavigationHandle with key of type ${key::class.java.simpleName} to TypedNavigationHandle<${T::class.java.simpleName}>")
    }
    return TypedNavigationHandleImpl(this, T::class.java)
}

fun <T> NavigationHandle.forward(key: T, vararg childKeys: NavigationKey) where T: NavigationKey, T: NavigationKey.SupportsForward =
    executeInstruction(NavigationInstruction.Forward(key, childKeys.toList()))

fun <T> NavigationHandle.present(key: T, vararg childKeys: NavigationKey) where T: NavigationKey, T: NavigationKey.SupportsPresent =
    executeInstruction(NavigationInstruction.Present(key, childKeys.toList()))

fun <T> NavigationHandle.replaceRoot(key: T, vararg childKeys: NavigationKey) where T: NavigationKey, T: NavigationKey.SupportsPresent =
    executeInstruction(NavigationInstruction.ReplaceRoot(key, childKeys.toList()))

fun NavigationHandle.close() =
    executeInstruction(NavigationInstruction.Close)

fun NavigationHandle.requestClose() =
    executeInstruction(NavigationInstruction.RequestClose)

internal fun NavigationHandle.runWhenHandleActive(block: () -> Unit) {
    val isMainThread = Looper.getMainLooper() == Looper.myLooper()
    if(isMainThread && lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
        block()
    } else {
        lifecycleScope.launchWhenCreated {
            block()
        }
    }
}