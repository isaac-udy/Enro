package dev.enro.core

import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import dev.enro.core.container.NavigationContainerContext
import dev.enro.core.controller.EnroDependencyScope
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.get
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

public interface NavigationHandle : LifecycleOwner {
    public val id: String
    public val key: NavigationKey
    public val instruction: NavigationInstruction.Open<*>
    public val dependencyScope: EnroDependencyScope
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
    override val instruction: NavigationInstruction.Open<*> = navigationHandle.instruction
    override val dependencyScope: EnroDependencyScope get() = navigationHandle.dependencyScope

    @Suppress("UNCHECKED_CAST")
    override val key: T get() = navigationHandle.key as? T
        ?: throw EnroException.IncorrectlyTypedNavigationHandle("TypedNavigationHandle failed to cast key of type ${navigationHandle.key::class.java.simpleName} to ${type.simpleName}")

    override val lifecycle: Lifecycle get() = navigationHandle.lifecycle

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

public fun NavigationHandle.push(key: NavigationKey.SupportsPush) {
    executeInstruction(NavigationInstruction.Push(key))
}

public fun NavigationHandle.push(key: NavigationKey.WithExtras<out NavigationKey.SupportsPush>) {
    executeInstruction(NavigationInstruction.Push(key))
}

public fun NavigationHandle.present(
    key: NavigationKey.SupportsPresent,
) {
    executeInstruction(NavigationInstruction.Present(key))
}

public fun NavigationHandle.present(key: NavigationKey.WithExtras<out NavigationKey.SupportsPresent>) {
    executeInstruction(NavigationInstruction.Present(key))
}

public fun NavigationHandle.replaceRoot(
    key: NavigationKey.SupportsPresent,
) {
    executeInstruction(NavigationInstruction.ReplaceRoot(key))
}

public fun NavigationHandle.replaceRoot(
    key: NavigationKey.WithExtras<out NavigationKey.SupportsPresent>,
) {
    executeInstruction(NavigationInstruction.ReplaceRoot(key))
}

public fun NavigationHandle.close() {
    executeInstruction(NavigationInstruction.Close)
}

public fun NavigationHandle.onContainer(
    key: NavigationContainerKey,
    block: NavigationContainerContext.() -> Unit
) {
    executeInstruction(NavigationInstruction.OnContainer(key, block))
}

public fun NavigationHandle.onActiveContainer(
    block: NavigationContainerContext.() -> Unit
) {
    executeInstruction(NavigationInstruction.OnActiveContainer(block))
}

public fun NavigationHandle.onParentContainer(
    block: NavigationContainerContext.() -> Unit
) {
    executeInstruction(NavigationInstruction.OnParentContainer(block))
}

public fun <T : Any> TypedNavigationHandle<out NavigationKey.WithResult<T>>.closeWithResult(result: T) {
    executeInstruction(NavigationInstruction.Close.WithResult(result))
}

public fun NavigationHandle.requestClose() {
    executeInstruction(NavigationInstruction.RequestClose)
}

internal fun NavigationHandle.runWhenHandleActive(block: () -> Unit) {
    val isMainThread = runCatching {
        Looper.getMainLooper() == Looper.myLooper()
    }.getOrElse { dependencyScope.get<NavigationController>().isInTest } // if the controller is in a Jvm only test, the block above may fail to run

    if(isMainThread && lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
        block()
    } else {
        lifecycleScope.launch {
            lifecycle.withCreated {
                block()
            }
        }
    }
}