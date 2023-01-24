package dev.enro.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import dagger.hilt.internal.GeneratedComponentManager
import dagger.hilt.internal.GeneratedComponentManagerHolder
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.destination.activity
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.NavigationContainerManager
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.navigationController
import dev.enro.core.internal.handle.getNavigationHandleViewModel

public sealed class NavigationContext<ContextType : Any>(
    public val contextReference: ContextType
) {
    public abstract val controller: NavigationController
    public abstract val parentContext: NavigationContext<*>?

    public abstract val lifecycle: Lifecycle
    public abstract val arguments: Bundle
    public abstract val viewModelStoreOwner: ViewModelStoreOwner
    public abstract val savedStateRegistryOwner: SavedStateRegistryOwner
    public abstract val lifecycleOwner: LifecycleOwner

    internal val binding: NavigationBinding<*, ContextType>? by lazy {
        controller.bindingForDestinationType(contextReference::class) as? NavigationBinding<*, ContextType>
    }

    public val containerManager: NavigationContainerManager = NavigationContainerManager()
}

internal class ActivityContext<ContextType : ComponentActivity>(
    contextReference: ContextType,
) : NavigationContext<ContextType>(contextReference) {
    override val controller get() = contextReference.application.navigationController
    override val lifecycle get() = contextReference.lifecycle
    override val parentContext: NavigationContext<*>? = null
    override val arguments: Bundle by lazy { contextReference.intent.extras ?: Bundle() }

    override val viewModelStoreOwner: ViewModelStoreOwner get() = contextReference
    override val savedStateRegistryOwner: SavedStateRegistryOwner get() = contextReference
    override val lifecycleOwner: LifecycleOwner get() = contextReference
}

internal class FragmentContext<ContextType : Fragment>(
    contextReference: ContextType,
) : NavigationContext<ContextType>(contextReference) {
    override val controller get() = contextReference.requireActivity().application.navigationController
    override val lifecycle get() = contextReference.lifecycle
    override val arguments: Bundle by lazy { contextReference.arguments ?: Bundle() }

    override val viewModelStoreOwner: ViewModelStoreOwner get() = contextReference
    override val savedStateRegistryOwner: SavedStateRegistryOwner get() = contextReference
    override val lifecycleOwner: LifecycleOwner get() = contextReference

    override val parentContext: NavigationContext<*>
        get() {
            return when (val parentFragment = fragment.parentFragment) {
                null -> fragment.requireActivity().navigationContext
                else -> parentFragment.navigationContext
            }
        }
}

internal class ComposeContext<ContextType : ComposableDestination>(
    contextReference: ContextType
) : NavigationContext<ContextType>(contextReference) {
    override val controller: NavigationController get() = contextReference.owner.activity.application.navigationController
    override val parentContext: NavigationContext<*> get() = contextReference.owner.parentContainer.parentContext
    override val lifecycle: Lifecycle get() = contextReference.owner.lifecycle
    override val arguments: Bundle by lazy { bundleOf(OPEN_ARG to contextReference.owner.instruction) }

    override val viewModelStoreOwner: ViewModelStoreOwner get() = contextReference
    override val savedStateRegistryOwner: SavedStateRegistryOwner get() = contextReference
    override val lifecycleOwner: LifecycleOwner get() = contextReference
}

public val NavigationContext<out Fragment>.fragment: Fragment get() = contextReference

public fun NavigationContext<*>.parentContainer(): NavigationContainer? {
    val parentContext = parentContext ?: return null

    val instructionId = getNavigationHandle().id
    fun getParentContainerFrom(context: NavigationContext<*>): NavigationContainer? {
        val parentContainer = context.containerManager.containers.firstOrNull { container ->
            container.backstackState.backstack.any { it.instructionId == instructionId }
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
        container.backstackState.backstack.any { it.instructionId == instructionId }
    }
}

public fun NavigationContext<*>.findRootContainer(): NavigationContainer? {
    var parentContainer = parentContainer()
    while(parentContainer != null) {
        parentContainer = parentContainer() ?: return parentContainer
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
            val childContext = container.activeContext ?: return@forEach
            val found = findFrom(childContext)
            if (found != null) return found
        }
        val parentContext = context.parentContext() ?: return null
        return findFrom(parentContext)
    }

    return findFrom(this)
}

public fun NavigationContext<*>.requireContainer(navigationContainerKey: NavigationContainerKey): NavigationContainer {
    return requireNotNull(findContainer(navigationContainerKey))
}


public val NavigationContext<*>.activity: ComponentActivity
    get() = when (contextReference) {
        is ComponentActivity -> contextReference
        is Fragment -> contextReference.requireActivity()
        is ComposableDestination -> contextReference.owner.activity
        else -> throw EnroException.UnreachableState()
    }

@PublishedApi
@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
internal val <T : ComponentActivity> T.navigationContext: ActivityContext<T>
    get() = getNavigationHandleViewModel().navigationContext as ActivityContext<T>

@PublishedApi
@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
internal val <T : Fragment> T.navigationContext: FragmentContext<T>
    get() = getNavigationHandleViewModel().navigationContext as FragmentContext<T>

@PublishedApi
@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
internal val <T : ComposableDestination> T.navigationContext: ComposeContext<T>
    get() = getNavigationHandleViewModel().navigationContext as ComposeContext<T>

@AdvancedEnroApi
internal val ViewModelStoreOwner.navigationContext: NavigationContext<*>?
    get() = getNavigationHandleViewModel().navigationContext

public fun NavigationContext<*>.rootContext(): NavigationContext<*> {
    var parent = this
    while (true) {
        val currentContext = parent
        parent = parent.parentContext() ?: return currentContext
    }
}

public fun NavigationContext<*>.parentContext(): NavigationContext<*>? {
    return when (this) {
        is ActivityContext -> null
        is FragmentContext<out Fragment> ->
            when (val parentFragment = fragment.parentFragment) {
                null -> fragment.requireActivity().navigationContext
                else -> parentFragment.navigationContext
            }
        is ComposeContext<out ComposableDestination> -> contextReference.owner.parentContainer.parentContext
    }
}

public fun NavigationContext<*>.leafContext(): NavigationContext<*> {
    // TODO This currently includes inactive contexts, should it only check for actual active contexts?
    val fragmentManager = when (contextReference) {
        is FragmentActivity -> contextReference.supportFragmentManager
        is Fragment -> contextReference.childFragmentManager
        else -> null
    }
    return containerManager.activeContainer?.activeContext?.leafContext()
        ?: fragmentManager?.primaryNavigationFragment?.navigationContext?.leafContext()
        ?: this
}

public val ComponentActivity.containerManager: NavigationContainerManager get() = navigationContext.containerManager
public val Fragment.containerManager: NavigationContainerManager get() = navigationContext.containerManager
public val ComposableDestination.containerManager: NavigationContainerManager get() = navigationContext.containerManager

public val containerManager: NavigationContainerManager
    @Composable
    get() {
        val viewModelStoreOwner = LocalViewModelStoreOwner.current!!
        return remember {
            viewModelStoreOwner
                .getNavigationHandleViewModel()
                .navigationContext!!
                .containerManager
        }
    }



private val generatedComponentManagerHolderClass  by lazy {
    runCatching {
        GeneratedComponentManagerHolder::class.java
    }.getOrNull()
}

internal val NavigationContext<*>.isHiltContext
    get() = if (generatedComponentManagerHolderClass != null) {
        activity is GeneratedComponentManagerHolder
    } else false

private val generatedComponentManagerClass  by lazy {
    runCatching {
        GeneratedComponentManager::class.java
    }.getOrNull()
}

internal val NavigationContext<*>.isHiltApplication
    get() = if (generatedComponentManagerClass != null) {
        activity.application is GeneratedComponentManager<*>
    } else false