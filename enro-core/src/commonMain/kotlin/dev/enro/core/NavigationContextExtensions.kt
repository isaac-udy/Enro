package dev.enro.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.NavigationContainerManager
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.ActiveNavigationHandleReference
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map


internal val NavigationContext<*>.allParentContexts: List<NavigationContext<*>>
    get() {
        val parents = mutableListOf<NavigationContext<*>>()
        var parent: NavigationContext<*>? = parentContext
        while (parent != null) {
            parents.add(parent)
            parent = parent.parentContext
        }
        return parents
    }

internal val NavigationContext<*>.isActive: Flow<Boolean>
    get() {
        val id = instruction.instructionId
        return controller.dependencyScope.get<ActiveNavigationHandleReference>()
            .activeNavigationIdFlow
            .map { it == id }
            .distinctUntilChanged()
    }


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

@AdvancedEnroApi
public fun NavigationContext<*>.directParentContainer(): NavigationContainer? {
    val parentContext = parentContext ?: return null
    val instructionId = getNavigationHandle().id
    return parentContext.containerManager.containers.firstOrNull { container ->
        container.backstack.any { it.instructionId == instructionId }
    }
}


public val parentContainer: NavigationContainer?
    @Composable
    get() {
        val viewModelStoreOwner = requireNotNull(LocalViewModelStoreOwner.current) {
            "Failed to get parentContainer in Composable: LocalViewModelStoreOwner was null"
        }
        return remember {
            viewModelStoreOwner
                .navigationContext
                ?.parentContainer()
        }
    }

public fun NavigationContext<*>.findRootContainer(): NavigationContainer? {
    var parentContainer = parentContainer()
    while(parentContainer != null) {
        val nextParent = parentContainer.context.parentContainer()
        if (nextParent == parentContainer) return parentContainer
        parentContainer = nextParent ?: return parentContainer
    }
    return null
}

public fun NavigationContext<*>.requireRootContainer(): NavigationContainer {
    return requireNotNull(findRootContainer())
}

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


public fun NavigationContext<*>.requireContainer(navigationContainerKey: NavigationContainerKey): NavigationContainer {
    return requireNotNull(findContainer(navigationContainerKey))
}


public fun NavigationContext<*>.rootContext(): NavigationContext<*> {
    var parent = this
    while (true) {
        val currentContext = parent
        parent = parent.parentContext ?: return currentContext
    }
}

public fun NavigationContext<*>.leafContext(): NavigationContext<*> {
    return containerManager.activeContainer?.childContext?.leafContext()
        ?: unboundChildContext?.leafContext()
        ?: this
}

internal val ViewModelStoreOwner.navigationContext: NavigationContext<*>?
    get() = getNavigationHandleViewModel().navigationContext

public val containerManager: NavigationContainerManager
    @Composable
    get() {
        val viewModelStoreOwner = requireNotNull(LocalViewModelStoreOwner.current) {
            "Failed to get containerManager in Composable: LocalViewModelStoreOwner was null"
        }
        val lifecycleOwner = LocalLifecycleOwner.current

        // The navigation context attached to a NavigationHandle may change when the ViewModelStoreOwner
        // or LifecycleOwner changes, so we're going to re-query the navigation context whenever
        // any of these change, to ensure the container always has an up-to-date NavigationContext
        return remember(viewModelStoreOwner, lifecycleOwner) {
            val navigationContext = requireNotNull(viewModelStoreOwner.navigationContext) {
                "Failed to get containerManager in Composable: LocalViewModelStoreOwner did not have a NavigationContext attached"
            }
            navigationContext.containerManager
        }
    }