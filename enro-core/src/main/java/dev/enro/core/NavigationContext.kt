package dev.enro.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.NavigationContainerManager
import dev.enro.core.controller.NavigationController
import dev.enro.core.usecase.requireNavigationContext

public abstract class NavigationContext<ContextType : Any> internal constructor(
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

public val NavigationContext<out Fragment>.fragment: Fragment get() = contextReference

public fun NavigationContext<*>.parentContainer(): NavigationContainer? {
    val parentContext = parentContext ?: return null
    val instructionId = this.getNavigationHandle().id
    return parentContext.containerManager.containers.firstOrNull {
        it.backstack.backstack.any { it.instructionId == instructionId }
    }
}

public val NavigationContext<*>.activity: ComponentActivity
    get() = when (contextReference) {
        is ComponentActivity -> contextReference
        is Fragment -> contextReference.requireActivity()
        is ComposableDestination -> contextReference.activity
        else -> throw EnroException.UnreachableState()
    }

@PublishedApi
@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
internal val <T : ComponentActivity> T.navigationContext: NavigationContext<T>
    get() = getNavigationHandle().requireNavigationContext() as NavigationContext<T>

@PublishedApi
@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
internal val <T : Fragment> T.navigationContext: NavigationContext<T>
    get() = getNavigationHandle().requireNavigationContext() as NavigationContext<T>

@PublishedApi
@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
internal val <T : ComposableDestination> T.navigationContext: NavigationContext<T>
    get() = getNavigationHandle().requireNavigationContext() as NavigationContext<T>

public fun NavigationContext<*>.rootContext(): NavigationContext<*> {
    var parent = this
    while (true) {
        val currentContext = parent
        parent = parent.parentContext ?: return currentContext
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

@PublishedApi
internal fun ViewModelStoreOwner.getNavigationContext(): NavigationContext<*> {
    return getNavigationHandle().requireNavigationContext()
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
                .getNavigationContext()
                .containerManager
        }
    }