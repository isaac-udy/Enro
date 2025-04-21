package dev.enro.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.NavigationContainerManager
import dev.enro.core.controller.EnroDependencyScope
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.ActiveNavigationHandleReference
import dev.enro.core.controller.usecase.GetNavigationBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.reflect.KClass


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

// Interface to be implemented by platform-specific navigation hosts
public interface NavigationHost {
    public fun accept(instruction: NavigationInstruction.Open<*>): Boolean = true
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


public fun NavigationContainer.parentContainer(): NavigationContainer? = context.parentContainer()

// Extension property for platform-specific ComposableDestination that implements ComposableDestinationReference
public val ComposableDestinationReference.parentContainer: NavigationContainer? 
    get() = navigationContext.parentContainer()

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

public fun NavigationContext<*>.rootContext(): NavigationContext<*> {
    var parent = this
    while (true) {
        val currentContext = parent
        parent = parent.parentContext ?: return currentContext
    }
}

// TODO this should be able to live in common
public expect fun NavigationContext<*>.leafContext(): NavigationContext<*>

/**
 * A NavigationHostFactory allows for destinations of different types to be interoperable with each other. For example,
 * a Fragment destination can host a Composable destination. There are two important functions to register here:
 * - supports: This function should return true if the NavigationHostFactory can host the provided NavigationInstruction.Open
 * - wrap: This function should return a new NavigationInstruction.Open that is compatible with the HostType
 */
@AdvancedEnroApi
public abstract class NavigationHostFactory<HostType: Any>(
    public val hostType: KClass<HostType>,
) {
    internal lateinit var dependencyScope: EnroDependencyScope

    private val getNavigationBinding: GetNavigationBinding by lazy { dependencyScope.get() }

    protected fun getNavigationBinding(instruction: NavigationInstruction.Open<*>): NavigationBinding<*, *>?
        = getNavigationBinding.invoke(instruction)

    protected fun requireNavigationBinding(instruction: NavigationInstruction.Open<*>): NavigationBinding<*, *>
            = getNavigationBinding.require(instruction)

    protected fun cannotCreateHost(instruction: NavigationInstruction.Open<*>): Nothing {
        throw EnroException.CannotCreateHostForType(hostType, instruction.internal.openingType)
    }

    public abstract fun supports(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>
    ): Boolean

    public abstract fun wrap(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>
    ): NavigationInstruction.Open<*>
}

// Extension to get navigation context from ViewModelStoreOwner, to be implemented per platform
internal expect val ViewModelStoreOwner.navigationContext: NavigationContext<*>?

public expect val containerManager: NavigationContainerManager
    @Composable
    get