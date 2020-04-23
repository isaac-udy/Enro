package nav.enro.core.internal.context

import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import nav.enro.core.NavigationKey
import nav.enro.core.internal.handle.NavigationHandleViewModel


internal fun NavigationContext<*, *>.rootContext(): NavigationContext<*, *> {
    var parent = this
    while (true) {
        val currentContext = parent
        parent = parent.parentContext()
        if (parent == currentContext) return parent
    }
}

internal fun NavigationContext<*, *>.parentContext(): NavigationContext<*, *> {
    return when (this) {
        is ActivityContext -> this
        is FragmentContext<out Fragment, *> ->
            when (val parentFragment = fragment.parentFragment) {
                null -> fragment.requireActivity().navigationContext
                else -> parentFragment.navigationContext
            }
    }
}

internal fun NavigationContext<*, out NavigationKey>.leafContext(): NavigationContext<*, out NavigationKey> {
    val primaryNavigationFragment = childFragmentManager.primaryNavigationFragment ?: return this
    val childContext = primaryNavigationFragment.navigationContext
    return childContext.leafContext()
}

@Suppress("UNCHECKED_CAST")
// If a the NavigationContext bound to an Activity is NOT an ActivityContext, the program should crash anyway - there's some other issue
internal val <T: FragmentActivity> T.navigationContext: ActivityContext<T, Nothing>
    get() = viewModels<NavigationHandleViewModel<Nothing>>().value.navigationContext as ActivityContext<T, Nothing>

@Suppress("UNCHECKED_CAST")
// If a the NavigationContext bound to a Fragment is NOT a FragmentContext, the program should crash anyway - there's some other issue
internal val <T: Fragment> T.navigationContext: FragmentContext<T, Nothing>
    get() = viewModels<NavigationHandleViewModel<Nothing>>().value.navigationContext as FragmentContext<T, Nothing>
