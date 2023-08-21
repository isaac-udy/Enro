package dev.enro.core

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.compatability.Compatibility
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.NavigationContainerManager
import dev.enro.core.controller.NavigationController
import dev.enro.core.internal.handle.getNavigationHandleViewModel

public class NavigationContext<ContextType : Any> internal constructor(
    public val contextReference: ContextType,
    private val getController: () -> NavigationController,
    private val getParentContext: () -> NavigationContext<*>?,
    private val getArguments: () -> Bundle,
    private val getViewModelStoreOwner: () -> ViewModelStoreOwner,
    private val getSavedStateRegistryOwner: () -> SavedStateRegistryOwner,
    private val getLifecycleOwner: () -> LifecycleOwner,
) {
    public val controller: NavigationController get() = getController()
    public val parentContext: NavigationContext<*>? get() = getParentContext()

    public val arguments: Bundle get() = getArguments()
    public val viewModelStoreOwner: ViewModelStoreOwner get() = getViewModelStoreOwner()
    public val savedStateRegistryOwner: SavedStateRegistryOwner get() = getSavedStateRegistryOwner()
    public val lifecycleOwner: LifecycleOwner get() = getLifecycleOwner()
    public val lifecycle: Lifecycle get() = lifecycleOwner.lifecycle

    public val containerManager: NavigationContainerManager = NavigationContainerManager()
}

internal val ViewModelStoreOwner.navigationContext: NavigationContext<*>?
    get() = getNavigationHandleViewModel().navigationContext

public fun NavigationContext<*>.rootContext(): NavigationContext<*> {
    var parent = this
    while (true) {
        val currentContext = parent
        parent = parent.parentContext ?: return currentContext
    }
}

public fun NavigationContext<*>.activeChildContext(): NavigationContext<*>? {
    return containerManager.activeContainer?.childContext
        ?: Compatibility.NavigationContext.leafContextFromFragment(this)
}

public fun NavigationContext<*>.leafContext(): NavigationContext<*> {
    // TODO This currently includes inactive contexts, should it only check for actual active contexts?
    return containerManager.activeContainer?.childContext?.leafContext()
        ?: Compatibility.NavigationContext.leafContextFromFragment(this)?.leafContext()
        ?: this
}

// Container extensions
public fun NavigationContext<*>.parentContainer(): NavigationContainer? {
    val parentContext = parentContext ?: return null

    val instructionId = when (contextReference) {
        is NavigationHost -> parentContext.containerManager.activeContainer?.backstack?.active?.instructionId ?: return null
        else -> getNavigationHandle().id
    }
    fun getParentContainerFrom(context: NavigationContext<*>): NavigationContainer? {
        val parentContainer = context.containerManager.containers.firstOrNull { container ->
            container.backstack.any { it.instructionId == instructionId }
        }
        val parentParent = runCatching { context.parentContext }.getOrNull() ?: return parentContainer
        return getParentContainerFrom(parentParent) ?: return parentContainer
    }

    return getParentContainerFrom(parentContext)
}
public fun NavigationContainer.parentContainer(): NavigationContainer? = context.parentContainer()

@AdvancedEnroApi
public fun NavigationContext<*>.directParentContainer(): NavigationContainer? {
    val parentContext = parentContext ?: return null
    val instructionId = getNavigationHandle().id
    return parentContext.containerManager.containers.firstOrNull { container ->
        container.backstack.any { it.instructionId == instructionId }
    }
}

public fun NavigationContext<*>.findRootContainer(): NavigationContainer? {
    if (contextReference is Activity) return containerManager.activeContainer

    var parentContainer = parentContainer()
    while(parentContainer != null) {
        val nextParent = parentContainer.parentContainer()
        if (nextParent == parentContainer) return parentContainer
        parentContainer = nextParent ?: return parentContainer
    }
    return null
}
public fun NavigationContainer.findRootContainer(): NavigationContainer? = context.findRootContainer()

public fun NavigationContext<*>.requireRootContainer(): NavigationContainer {
    return requireNotNull(findRootContainer())
}
public fun NavigationContainer.requireRootContainer(): NavigationContainer = context.requireRootContainer()

public fun NavigationContext<*>.findContainer(navigationContainerKey: NavigationContainerKey): NavigationContainer? {
    val seen = mutableSetOf<NavigationContext<*>>()

    fun findFrom(context: NavigationContext<*>): NavigationContainer? {
        if (seen.contains(context)) return null
        seen.add(context)

        val activeContainer = context.parentContainer()
        if (activeContainer != null) {
            if (activeContainer.key == navigationContainerKey) return activeContainer
        }
        context.containerManager.containers.forEach { container ->
            if (container.key == navigationContainerKey) return container
            val childContext = container.childContext ?: return@forEach
            val found = findFrom(childContext)
            if (found != null) return found
        }
        val parentContext = context.parentContext ?: return null
        return findFrom(parentContext)
    }

    return findFrom(this)
}
public fun NavigationContainer.findContainer(navigationContainerKey: NavigationContainerKey): NavigationContainer? = context.findContainer(navigationContainerKey)

public fun NavigationContext<*>.requireContainer(navigationContainerKey: NavigationContainerKey): NavigationContainer {
    return requireNotNull(findContainer(navigationContainerKey))
}
public fun NavigationContainer.requireContainer(navigationContainerKey: NavigationContainerKey): NavigationContainer = context.requireContainer(navigationContainerKey)


