package dev.enro.core

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import dagger.hilt.internal.GeneratedComponentManager
import dagger.hilt.internal.GeneratedComponentManagerHolder
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.destination.activity
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.NavigationContainerManager
import dev.enro.core.controller.NavigationController
import dev.enro.core.internal.handle.getNavigationHandleViewModel

/**
 * NavigationContext represents a context in which navigation can occur. In Android, this may be a Fragment, Activity, or Composable.
 *
 * When constructing a NavigationContext, the contextReference is the actual object that the NavigationContext represents
 * (e.g. a Fragment, Activity or Composable), and the other parameters are functions that can be used to retrieve information
 * about the context. The get functions are invoked lazily, either when the are accessed for the first time,
 * or once the NavigationContext is bound to a NavigationHandle.
 */
public class NavigationContext<ContextType : Any> internal constructor(
    public val contextReference: ContextType,
    private val getController: () -> NavigationController,
    private val getParentContext: () -> NavigationContext<*>?,
    private val getArguments: () -> Bundle,
    private val getViewModelStoreOwner: () -> ViewModelStoreOwner,
    private val getSavedStateRegistryOwner: () -> SavedStateRegistryOwner,
    private val getLifecycleOwner: () -> LifecycleOwner,
) {
    public val controller: NavigationController by lazy { getController() }
    public val parentContext: NavigationContext<*>? by lazy { getParentContext() }

    /**
     * The arguments provided to this NavigationContext. It is possible to read the open instruction from these arguments,
     * but it may be different than the open instruction attached to the NavigationHandle. If the arguments do not contain
     * a NavigationInstruction, a NavigationInstruction is still provided to the NavigationHandle, which will be either a
     * default key (if one is provided with the destination) or a "NoNavigationKey" NavigationKey.
     *
     * Generally it should be preferred to read the instruction property, rather than read the instruction from the arguments.
     */
    @AdvancedEnroApi
    public val arguments: Bundle by lazy { getArguments() }

    private lateinit var _instruction: NavigationInstruction.Open<*>
    public val instruction: NavigationInstruction.Open<*> get() = _instruction

    public val viewModelStoreOwner: ViewModelStoreOwner by lazy { getViewModelStoreOwner() }
    public val savedStateRegistryOwner: SavedStateRegistryOwner by lazy { getSavedStateRegistryOwner() }
    public val lifecycleOwner: LifecycleOwner by lazy { getLifecycleOwner() }
    public val lifecycle: Lifecycle get() = lifecycleOwner.lifecycle

    public val containerManager: NavigationContainerManager = NavigationContainerManager()

    private var _navigationHandle: NavigationHandle? = null
    public val navigationHandle: NavigationHandle get() = requireNotNull(_navigationHandle)

    internal fun bind(navigationHandle: NavigationHandle) {
        _navigationHandle = navigationHandle
        _instruction = navigationHandle.instruction

        // Invoke hashcode on all lazy items to ensure they are initialized

        controller.hashCode()
        parentContext.hashCode()
        arguments.hashCode()
        viewModelStoreOwner.hashCode()
        savedStateRegistryOwner.hashCode()
        lifecycleOwner.hashCode()
    }
}

public val NavigationContext<out Fragment>.fragment: Fragment get() = contextReference

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

public val NavigationContext<*>.activity: ComponentActivity
    get() = when (contextReference) {
        is ComponentActivity -> contextReference
        is Fragment -> contextReference.requireActivity()
        is ComposableDestination -> contextReference.owner.activity
        else -> throw EnroException.UnreachableState()
    }

@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
public val <T : ComponentActivity> T.navigationContext: NavigationContext<T>
    get() = getNavigationHandleViewModel().navigationContext as NavigationContext<T>

@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
public val <T : Fragment> T.navigationContext: NavigationContext<T>
    get() = getNavigationHandleViewModel().navigationContext as NavigationContext<T>

@PublishedApi
@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
internal val <T : ComposableDestination> T.navigationContext: NavigationContext<T>
    get() = context as NavigationContext<T>

public val navigationContext: NavigationContext<*>
    @Composable
    get() {
        if (LocalInspectionMode.current) error("Not able to access navigationContext when LocalInspectionMode.current is 'true'")

        val viewModelStoreOwner = requireNotNull(LocalViewModelStoreOwner.current) {
            "Failed to get navigationContext in Composable: LocalViewModelStoreOwner was null"
        }
        return remember(viewModelStoreOwner) {
            requireNotNull(viewModelStoreOwner.navigationContext) {
                "Failed to get navigationContext in Composable: ViewModelStore owner does not have a NavigationContext reference"
            }
        }
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
    val fragmentManager = when (contextReference) {
        is FragmentActivity -> contextReference.supportFragmentManager
        is Fragment -> contextReference.childFragmentManager
        else -> null
    }
    return containerManager.activeContainer?.childContext
        ?: fragmentManager?.primaryNavigationFragment?.navigationContext
}

public fun NavigationContext<*>.leafContext(): NavigationContext<*> {
    // TODO This currently includes inactive contexts, should it only check for actual active contexts?
    val fragmentManager = when (contextReference) {
        is FragmentActivity -> contextReference.supportFragmentManager
        is Fragment -> contextReference.childFragmentManager
        else -> null
    }
    return containerManager.activeContainer?.childContext?.leafContext()
        ?: runCatching { fragmentManager?.primaryNavigationFragment?.navigationContext }.getOrNull()?.leafContext()
        ?: this
}
public val ComponentActivity.containerManager: NavigationContainerManager get() = navigationContext.containerManager
public val Fragment.containerManager: NavigationContainerManager get() = navigationContext.containerManager
public val ComposableDestination.containerManager: NavigationContainerManager get() = navigationContext.containerManager

public val containerManager: NavigationContainerManager
    @Composable
    get() {
        val viewModelStoreOwner = LocalViewModelStoreOwner.current!!

        val context = LocalContext.current
        val view = LocalView.current
        val lifecycleOwner = LocalLifecycleOwner.current

        // The navigation context attached to a NavigationHandle may change when the Context, View,
        // or LifecycleOwner changes, so we're going to re-query the navigation context whenever
        // any of these change, to ensure the container always has an up-to-date NavigationContext
        return remember(context, view, lifecycleOwner) {
            viewModelStoreOwner
                .navigationContext!!
                .containerManager
        }
    }

public val Fragment.parentContainer: NavigationContainer? get() = navigationContext.parentContainer()
public val ComposableDestination.parentContainer: NavigationContainer? get() = navigationContext.parentContainer()

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