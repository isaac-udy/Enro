package dev.enro.core

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
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
    internal val navigationHandle: NavigationHandle
): TypedNavigationHandle<T> {
    override val id: String get() = navigationHandle.id
    override val controller: NavigationController get() = navigationHandle.controller
    override val additionalData: Bundle get() = navigationHandle.additionalData
    override val key: T get() = navigationHandle.key as T

    override fun getLifecycle(): Lifecycle = navigationHandle.lifecycle

    override fun executeInstruction(navigationInstruction: NavigationInstruction) = navigationHandle.executeInstruction(navigationInstruction)
}

fun <T: NavigationKey> NavigationHandle.asTyped(type: KClass<T>): TypedNavigationHandle<T> {
    val keyType = key::class
    val isValidType = type.java.isAssignableFrom(keyType.java)
    if(!isValidType) {
        throw IllegalStateException("Failed to cast NavigationHandle with key $key to TypedNavigationHandle<${type.simpleName}>")
    }
    return TypedNavigationHandleImpl(this)
}

inline fun <reified T: NavigationKey> NavigationHandle.asTyped(): TypedNavigationHandle<T> {
    if(key !is T) {
        throw IllegalStateException("Failed to cast NavigationHandle with key $key to TypedNavigationHandle<${T::class.java.simpleName}>")
    }
    return TypedNavigationHandleImpl(this)
}

fun NavigationHandle.forward(key: NavigationKey, vararg childKeys: NavigationKey) =
    executeInstruction(NavigationInstruction.Forward(key, childKeys.toList()))

fun NavigationHandle.replace(key: NavigationKey, vararg childKeys: NavigationKey) =
    executeInstruction(NavigationInstruction.Replace(key, childKeys.toList()))

fun NavigationHandle.replaceRoot(key: NavigationKey, vararg childKeys: NavigationKey) =
    executeInstruction(NavigationInstruction.ReplaceRoot(key, childKeys.toList()))

fun NavigationHandle.close() =
    executeInstruction(NavigationInstruction.Close)

fun NavigationHandle.requestClose() =
    executeInstruction(NavigationInstruction.RequestClose)