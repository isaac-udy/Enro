package nav.enro.core.internal.context

import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import nav.enro.core.NavigationKey
import nav.enro.core.internal.handle.NavigationHandleViewModel


internal fun NavigationContext<*>.rootContext(): NavigationContext<*> {
    var parent = this
    while (true) {
        val currentContext = parent
        parent = parent.parentContext()
        if (parent == currentContext) return parent
    }
}

internal fun NavigationContext<*>.parentContext(): NavigationContext<*> {
    return when (this) {
        is ActivityContext -> this
        is FragmentContext ->
            when (val parentFragment = fragment.parentFragment) {
                null -> fragment.requireActivity().navigationContext
                else -> parentFragment.navigationContext
            }
    }
}

internal fun NavigationContext<out NavigationKey>.leafContext(): NavigationContext<out NavigationKey> {
    when (this) {
        is ActivityContext -> {
            val fragment = activeFragmentHost?.let {
                it.fragmentManager.findFragmentById(it.containerView)
            } ?: return this

            val childContext = fragment.navigationContext
            return childContext.leafContext()
        }
        is FragmentContext -> {
            val childHost = activeFragmentHost ?: return this
            val fragment = childHost.fragmentManager.findFragmentById(childHost.containerView)
                ?: return this

            val childContext = fragment.navigationContext
            return childContext.leafContext()
        }
    }
}

internal val FragmentActivity.navigationContext: ActivityContext<Nothing>
    get() = viewModels<NavigationHandleViewModel<Nothing>>().value.navigationContext!! as ActivityContext<Nothing>

internal val Fragment.navigationContext: FragmentContext<Nothing>
    get() = viewModels<NavigationHandleViewModel<Nothing>>().value.navigationContext!! as FragmentContext<Nothing>
