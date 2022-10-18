package dev.enro.core

import android.os.Bundle
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import dev.enro.core.controller.NavigationController
import kotlin.reflect.KClass

public interface NavigationHandle : LifecycleOwner {
    public val id: String
    public val controller: NavigationController
    public val additionalData: Bundle
    public val key: NavigationKey
    public val instruction: NavigationInstruction.Open<*>
    public fun executeInstruction(navigationInstruction: NavigationInstruction)
}

public interface TypedNavigationHandle<T : NavigationKey> : NavigationHandle {
    override val key: T
}

@PublishedApi
internal class TypedNavigationHandleImpl<T : NavigationKey>(
    internal val navigationHandle: NavigationHandle,
    private val type: Class<T>
): TypedNavigationHandle<T> {
    override val id: String get() = navigationHandle.id
    override val controller: NavigationController get() = navigationHandle.controller
    override val additionalData: Bundle get() = navigationHandle.additionalData
    override val instruction: NavigationInstruction.Open<*> = navigationHandle.instruction

    @Suppress("UNCHECKED_CAST")
    override val key: T get() = navigationHandle.key as? T
        ?: throw EnroException.IncorrectlyTypedNavigationHandle("TypedNavigationHandle failed to cast key of type ${navigationHandle.key::class.java.simpleName} to ${type.simpleName}")

    override fun getLifecycle(): Lifecycle = navigationHandle.lifecycle

    override fun executeInstruction(navigationInstruction: NavigationInstruction) = navigationHandle.executeInstruction(navigationInstruction)
}

public fun <T : NavigationKey> NavigationHandle.asTyped(type: KClass<T>): TypedNavigationHandle<T> {
    val keyType = key::class
    val isValidType = type.java.isAssignableFrom(keyType.java)
    if (!isValidType) {
        throw EnroException.IncorrectlyTypedNavigationHandle("Failed to cast NavigationHandle with key of type ${keyType.java.simpleName} to TypedNavigationHandle<${type.simpleName}>")
    }

    @Suppress("UNCHECKED_CAST")
    if (this is TypedNavigationHandleImpl<*>) return this as TypedNavigationHandle<T>
    return TypedNavigationHandleImpl(this, type.java)
}

public inline fun <reified T : NavigationKey> NavigationHandle.asTyped(): TypedNavigationHandle<T> {
    if (key !is T) {
        throw EnroException.IncorrectlyTypedNavigationHandle("Failed to cast NavigationHandle with key of type ${key::class.java.simpleName} to TypedNavigationHandle<${T::class.java.simpleName}>")
    }
    return TypedNavigationHandleImpl(this, T::class.java)
}

public fun NavigationHandle.push(key: NavigationKey.SupportsPush, vararg childKeys: NavigationKey) {
    executeInstruction(NavigationInstruction.Push(key, childKeys.toList()))
}

public fun NavigationHandle.present(
    key: NavigationKey.SupportsPresent,
    vararg childKeys: NavigationKey
) {
    executeInstruction(NavigationInstruction.Present(key, childKeys.toList()))
}

public fun NavigationHandle.replaceRoot(
    key: NavigationKey.SupportsPresent,
    vararg childKeys: NavigationKey
) {
    executeInstruction(NavigationInstruction.ReplaceRoot(key, childKeys.toList()))
}

@Deprecated("You should use push or present")
public fun NavigationHandle.forward(key: NavigationKey, vararg childKeys: NavigationKey) {
    executeInstruction(NavigationInstruction.Forward(key, childKeys.toList()))
}

@Deprecated("You should use a close instruction followed by a push or present")
public fun NavigationHandle.replace(key: NavigationKey, vararg childKeys: NavigationKey) {
    executeInstruction(NavigationInstruction.Replace(key, childKeys.toList()))
}

@Deprecated("You should only use replaceRoot with a NavigationKey.SupportsPresent")
public fun NavigationHandle.replaceRoot(key: NavigationKey, vararg childKeys: NavigationKey) {
    executeInstruction(NavigationInstruction.ReplaceRoot(key, childKeys.toList()))
}

public fun NavigationHandle.close() {
    executeInstruction(NavigationInstruction.Close)
}

public fun NavigationHandle.requestClose() {
    executeInstruction(NavigationInstruction.RequestClose)
}

public val NavigationHandle.isPushed: Boolean
    get() = instruction.navigationDirection == NavigationDirection.Push

public val NavigationHandle.isPresented: Boolean
    get() = instruction.navigationDirection == NavigationDirection.Present || instruction.navigationDirection == NavigationDirection.ReplaceRoot

internal fun NavigationHandle.runWhenHandleActive(block: () -> Unit) {
    val isMainThread = runCatching {
        Looper.getMainLooper() == Looper.myLooper()
    }.getOrElse { controller.isInTest } // if the controller is in a Jvm only test, the block above may fail to run

    if(isMainThread && lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
        block()
    } else {
        lifecycleScope.launchWhenCreated {
            block()
        }
    }
}