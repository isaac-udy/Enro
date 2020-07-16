package nav.enro.core.context

import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import nav.enro.core.NavigationKey
import nav.enro.core.internal.handle.NavigationHandleViewModel
import nav.enro.core.navigator.FragmentHost
import java.lang.IllegalStateException

val NavigationContext<out Fragment, *>.fragment get() = contextReference
val NavigationContext<*, *>.activity: FragmentActivity
    get() = when (contextReference) {
        is FragmentActivity -> contextReference
        is Fragment -> contextReference.requireActivity()
        else -> throw IllegalStateException()
    }

internal fun NavigationContext<*, *>.fragmentHostFor(key: NavigationKey): FragmentHost? {
    val primaryFragment = childFragmentManager.primaryNavigationFragment
    val activeContainerId = primaryFragment?.getContainerId()

    val visibleContainers = childContainers.filter {
        when (contextReference) {
            is FragmentActivity -> contextReference.findViewById<View>(it.containerId).isVisible
            is Fragment -> contextReference.requireView().findViewById<View>(it.containerId).isVisible
            else -> false
        }
    }

    val primaryDefinition = visibleContainers.firstOrNull {
        it.containerId == activeContainerId && it.accept(key)
    }
    val definition = primaryDefinition
        ?: visibleContainers.firstOrNull { it.accept(key) }

    return definition?.let {
        FragmentHost(
            containerId = it.containerId,
            fragmentManager = childFragmentManager
        )
    } ?: parentContext()?.fragmentHostFor(key)
}

internal fun Fragment.getContainerId() = (requireView().parent as View).id

fun NavigationContext<*, *>.rootContext(): NavigationContext<*, *> {
    var parent = this
    while (true) {
        val currentContext = parent
        parent = parent.parentContext() ?: return currentContext
    }
}

fun NavigationContext<*, *>.parentContext(): NavigationContext<*, *>? {
    return when (this) {
        is ActivityContext -> null
        is FragmentContext<out Fragment, *> ->
            when (val parentFragment = fragment.parentFragment) {
                null -> fragment.requireActivity().navigationContext
                else -> parentFragment.navigationContext
            }
    }
}

fun NavigationContext<*, out NavigationKey>.leafContext(): NavigationContext<*, out NavigationKey> {
    val primaryNavigationFragment = childFragmentManager.primaryNavigationFragment ?: return this
    primaryNavigationFragment.view ?: return this
    val childContext = primaryNavigationFragment.navigationContext
    return childContext.leafContext()
}

@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
internal val <T : FragmentActivity> T.navigationContext: ActivityContext<T, Nothing>
    get() = viewModels<NavigationHandleViewModel<Nothing>> { ViewModelProvider.NewInstanceFactory() } .value.navigationContext as ActivityContext<T, Nothing>

@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
internal val <T : Fragment> T.navigationContext: FragmentContext<T, Nothing>
    get() = viewModels<NavigationHandleViewModel<Nothing>> { ViewModelProvider.NewInstanceFactory() } .value.navigationContext as FragmentContext<T, Nothing>