package dev.enro.core

import android.app.Activity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.core.activity.ActivityNavigator
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.EnroComposableManager
import dev.enro.core.compose.composableManger
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
    abstract val childFragmentManager: FragmentManager
    abstract val childComposableManager: EnroComposableManager
    abstract val arguments: Bundle
    abstract val viewModelStoreOwner: ViewModelStoreOwner
    abstract val savedStateRegistryOwner: SavedStateRegistryOwner
    abstract val lifecycleOwner: LifecycleOwner

    internal open val navigator: Navigator<*, ContextType>? by lazy {
        controller.navigatorForContextType(contextReference::class) as? Navigator<*, ContextType>
    }
}

internal class ActivityContext<ContextType : FragmentActivity>(
    contextReference: ContextType,
) : NavigationContext<ContextType>(contextReference) {
    override val controller get() = contextReference.application.navigationController
    override val lifecycle get() = contextReference.lifecycle
    override val navigator get() = super.navigator as? ActivityNavigator<*, ContextType>
    override val childFragmentManager get() = contextReference.supportFragmentManager
    override val childComposableManager: EnroComposableManager get() = contextReference.composableManger
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
    override val childFragmentManager get() = contextReference.childFragmentManager
    override val childComposableManager: EnroComposableManager get() = contextReference.composableManger
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
    override val childFragmentManager: FragmentManager get() = contextReference.contextReference.activity.supportFragmentManager
    override val childComposableManager: EnroComposableManager get() = contextReference.contextReference.composableManger
    override val arguments: Bundle by lazy { bundleOf(OPEN_ARG to contextReference.contextReference.instruction) }

    override val viewModelStoreOwner: ViewModelStoreOwner get() = contextReference
    override val savedStateRegistryOwner: SavedStateRegistryOwner get() = contextReference
    override val lifecycleOwner: LifecycleOwner get() = contextReference
}

val NavigationContext<out Fragment>.fragment get() = contextReference

val NavigationContext<*>.activity: FragmentActivity
    get() = when (contextReference) {
        is FragmentActivity -> contextReference
        is Fragment -> contextReference.requireActivity()
        is ComposableDestination -> contextReference.contextReference.activity
        else -> throw IllegalStateException()
    }

@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
internal val <T : FragmentActivity> T.navigationContext: ActivityContext<T>
    get() = viewModels<NavigationHandleViewModel> { ViewModelProvider.NewInstanceFactory() } .value.navigationContext as ActivityContext<T>

@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
internal val <T : Fragment> T.navigationContext: FragmentContext<T>
    get() = viewModels<NavigationHandleViewModel> { ViewModelProvider.NewInstanceFactory() } .value.navigationContext as FragmentContext<T>

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
        is ComposeContext<out ComposableDestination> -> contextReference.contextReference.requireParentContainer().navigationContext
    }
}

fun NavigationContext<*>.leafContext(): NavigationContext<*> {
    return when(this) {
        is ActivityContext,
        is FragmentContext -> {
            val primaryNavigationFragment = childFragmentManager.primaryNavigationFragment
                ?: return childComposableManager.activeContainer?.activeContext?.leafContext() ?: this
            primaryNavigationFragment.view ?: return this
            primaryNavigationFragment.navigationContext.leafContext()
        }
        is ComposeContext<*> -> childComposableManager.activeContainer?.activeContext?.leafContext() ?: this
    }
}

internal fun NavigationContext<*>.getNavigationHandleViewModel(): NavigationHandleViewModel {
    return when (this) {
        is FragmentContext<out Fragment> -> fragment.getNavigationHandle()
        is ActivityContext<out FragmentActivity> -> activity.getNavigationHandle()
        is ComposeContext<out ComposableDestination> -> contextReference.contextReference.getNavigationHandleViewModel()
    } as NavigationHandleViewModel
}