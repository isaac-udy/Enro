package dev.enro.core

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import dev.enro.core.container.NavigationContainerContext
import dev.enro.core.controller.EnroDependencyScope
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.get
import dev.enro.core.internal.EnroLog
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import dev.enro.core.internal.isMainThread
import kotlinx.coroutines.flow.first
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
    private val type: KClass<T>
) : TypedNavigationHandle<T> {
    override val id: String get() = navigationHandle.id
    override val instruction: NavigationInstruction.Open<*> = navigationHandle.instruction
    override val dependencyScope: EnroDependencyScope get() = navigationHandle.dependencyScope

    @Suppress("UNCHECKED_CAST")
    override val key: T
        get() = navigationHandle.key as? T
            ?: throw EnroException.IncorrectlyTypedNavigationHandle("TypedNavigationHandle failed to cast key of type ${navigationHandle.key::class.simpleName} to ${type.simpleName}")

    override val lifecycle: Lifecycle get() = navigationHandle.lifecycle

    override fun executeInstruction(navigationInstruction: NavigationInstruction) =
        navigationHandle.executeInstruction(navigationInstruction)
}

public fun <T : NavigationKey> NavigationHandle.asTyped(type: KClass<T>): TypedNavigationHandle<T> {
    val isValidType = type.isInstance(key)
    if (!isValidType) {
        throw EnroException.IncorrectlyTypedNavigationHandle("Failed to cast NavigationHandle with key of type ${key::class.simpleName} to TypedNavigationHandle<${type.simpleName}>")
    }

    @Suppress("UNCHECKED_CAST")
    if (this is TypedNavigationHandleImpl<*>) return this as TypedNavigationHandle<T>
    return TypedNavigationHandleImpl(this, type)
}

public inline fun <reified T : NavigationKey> NavigationHandle.asTyped(): TypedNavigationHandle<T> {
    if (key !is T) {
        throw EnroException.IncorrectlyTypedNavigationHandle("Failed to cast NavigationHandle with key of type ${key::class.simpleName} to TypedNavigationHandle<${T::class.simpleName}>")
    }
    return TypedNavigationHandleImpl(this, T::class)
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

public fun NavigationHandle.close() {
    executeInstruction(NavigationInstruction.Close)
}

public fun NavigationHandle.closeAndPush(key: NavigationKey.SupportsPush) {
    executeInstruction(NavigationInstruction.Close.AndThenOpen(NavigationInstruction.Push(key)))
}

public fun NavigationHandle.closeAndPush(key: NavigationKey.WithExtras<out NavigationKey.SupportsPush>) {
    executeInstruction(NavigationInstruction.Close.AndThenOpen(NavigationInstruction.Push(key)))
}

public fun NavigationHandle.closeAndPresent(
    key: NavigationKey.SupportsPresent,
) {
    executeInstruction(NavigationInstruction.Close.AndThenOpen(NavigationInstruction.Present(key)))
}

public fun NavigationHandle.closeAndPresent(key: NavigationKey.WithExtras<out NavigationKey.SupportsPresent>) {
    executeInstruction(NavigationInstruction.Close.AndThenOpen(NavigationInstruction.Present(key)))
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

@Deprecated("Use the NavigationController's WindowManager to close or open specific windows")
public fun NavigationHandle.replaceRoot(navigationKey: NavigationKey) {
    val context =  requireNavigationContext()
    val rootContext = context.rootContext()
    context.controller.windowManager.close(
        context = rootContext,
        andOpen = NavigationInstruction.DefaultDirection(navigationKey),
    )
}

@Deprecated("Use closeAndPush or closeAndPresent instead")
public fun NavigationHandle.replace(
    navigationKey: NavigationKey,
) {
    executeInstruction(NavigationInstruction.Close.AndThenOpen(NavigationInstruction.DefaultDirection(navigationKey)))
}

public fun <T : Any> TypedNavigationHandle<out NavigationKey.WithResult<T>>.closeWithResult(result: T) {
    EnroLog.error("Requesting close from ${getNavigationContext()?.toDisplayString()} with ${getNavigationContext()?.parentContainer()} ${getNavigationContext()?.lifecycle?.currentState}")
    executeInstruction(NavigationInstruction.Close.WithResult(result))
}

public fun NavigationHandle.requestClose() {
    executeInstruction(NavigationInstruction.RequestClose)
}

internal fun NavigationHandle.runWhenHandleActive(block: () -> Unit) {
    val isMainThread = runCatching {
        isMainThread()
    }.getOrElse { enroConfig.isInTest } // if the controller is in a Jvm only test, the block above may fail to run

    if (isMainThread && lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
        block()
    } else {
        lifecycleScope.launch {
            lifecycle.currentStateFlow.first { it.isAtLeast(Lifecycle.State.CREATED) }
            block()
        }
    }
}

internal val NavigationHandle.enroConfig: EnroConfig
    get() = runCatching {
        dependencyScope.get<NavigationController>().config
    }.getOrElse { EnroConfig() }


internal fun NavigationHandle.getParentNavigationHandle() : NavigationHandle? {
    var parentContext = getNavigationContext()?.parentContext
    if (parentContext?.contextReference is NavigationHost) {
        parentContext = parentContext.parentContext
    }
    return parentContext?.getNavigationHandle()
}

public fun ViewModelStoreOwner.getNavigationHandle(): NavigationHandle {
    return getNavigationHandleViewModel()
}

