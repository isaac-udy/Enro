package dev.enro.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.core.activity.ActivityNavigator
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.container.NavigationContainerManager
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.navigationController
import dev.enro.core.fragment.FragmentNavigator
import dev.enro.core.internal.handle.NavigationHandleViewModel
import dev.enro.core.internal.handle.getNavigationHandleViewModel

sealed class NavigationContext<ContextType : Any>(
    val contextReference: ContextType
) {
    abstract val controller: NavigationController
    abstract val lifecycle: Lifecycle
    abstract val arguments: Bundle
    abstract val viewModelStoreOwner: ViewModelStoreOwner
    abstract val savedStateRegistryOwner: SavedStateRegistryOwner
    abstract val lifecycleOwner: LifecycleOwner

    internal open val navigator: Navigator<*, ContextType>? by lazy {
        controller.navigatorForContextType(contextReference::class) as? Navigator<*, ContextType>
    }

    val containerManager: NavigationContainerManager = NavigationContainerManager()
}

internal class ActivityContext<ContextType : ComponentActivity>(
    contextReference: ContextType,
) : NavigationContext<ContextType>(contextReference) {
    override val controller get() = contextReference.application.navigationController
    override val lifecycle get() = contextReference.lifecycle
    override val navigator get() = super.navigator as? ActivityNavigator<*, ContextType>
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
    override val navigator get() = super.navigator as? FragmentNavigator<*, ContextType>
    override val arguments: Bundle by lazy { contextReference.arguments ?: Bundle() }

    override val viewModelStoreOwner: ViewModelStoreOwner get() = contextReference
    override val savedStateRegistryOwner: SavedStateRegistryOwner get() = contextReference
    override val lifecycleOwner: LifecycleOwner get() = contextReference
}

internal class ComposeContext<ContextType : ComposableDestination>(
    contextReference: ContextType
) : NavigationContext<ContextType>(contextReference) {
    override val controller: NavigationController get() = contextReference.contextReference.activity.application.navigationController
    override val lifecycle: Lifecycle get() = contextReference.contextReference.lifecycle
    override val arguments: Bundle by lazy { bundleOf(OPEN_ARG to contextReference.contextReference.instruction) }

    override val viewModelStoreOwner: ViewModelStoreOwner get() = contextReference
    override val savedStateRegistryOwner: SavedStateRegistryOwner get() = contextReference
    override val lifecycleOwner: LifecycleOwner get() = contextReference
}

val NavigationContext<out Fragment>.fragment get() = contextReference

val NavigationContext<*>.activity: ComponentActivity
    get() = when (contextReference) {
        is ComponentActivity -> contextReference
        is Fragment -> contextReference.requireActivity()
        is ComposableDestination -> contextReference.contextReference.activity
        else -> throw IllegalStateException()
    }

@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
internal val <T : ComponentActivity> T.navigationContext: ActivityContext<T>
    get() = getNavigationHandleViewModel().navigationContext as ActivityContext<T>

@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
internal val <T : Fragment> T.navigationContext: FragmentContext<T>
    get() = getNavigationHandleViewModel().navigationContext as FragmentContext<T>

@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
internal val <T : ComposableDestination> T.navigationContext: ComposeContext<T>
    get() = getNavigationHandleViewModel().navigationContext as ComposeContext<T>

fun NavigationContext<*>.rootContext(): NavigationContext<*> {
    var parent = this
    while (true) {
        val currentContext = parent
        parent = parent.parentContext() ?: return currentContext
    }
}

fun NavigationContext<*>.parentContext(): NavigationContext<*>? {
    return when (this) {
        is ActivityContext -> null
        is FragmentContext<out Fragment> ->
            when (val parentFragment = fragment.parentFragment) {
                null -> fragment.requireActivity().navigationContext
                else -> parentFragment.navigationContext
            }
        is ComposeContext<out ComposableDestination> -> contextReference.contextReference.requireParentContainer().parentContext
    }
}

fun NavigationContext<*>.leafContext(): NavigationContext<*> {
    return containerManager.activeContainer?.activeContext?.leafContext() ?: this
}

internal fun NavigationContext<*>.getNavigationHandleViewModel(): NavigationHandleViewModel {
    return when (this) {
        is FragmentContext<out Fragment> -> fragment.getNavigationHandle()
        is ActivityContext<out ComponentActivity> -> activity.getNavigationHandle()
        is ComposeContext<out ComposableDestination> -> contextReference.contextReference.getNavigationHandleViewModel()
    } as NavigationHandleViewModel
}

val ComponentActivity.containerManager: NavigationContainerManager get() = navigationContext.containerManager
val Fragment.containerManager: NavigationContainerManager get() = navigationContext.containerManager
val ComposableDestination.containerManager: NavigationContainerManager get() = navigationContext.containerManager