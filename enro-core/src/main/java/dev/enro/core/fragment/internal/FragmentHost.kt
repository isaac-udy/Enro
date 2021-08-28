package dev.enro.core.fragment.internal

import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationKey
import dev.enro.core.getNavigationHandleViewModel
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import dev.enro.core.parentContext

internal class FragmentHost(
    internal val containerId: Int,
    internal val fragmentManager: FragmentManager,
    internal val accept: (NavigationKey) -> Boolean
)

internal fun NavigationContext<*>.fragmentHostFor(key: NavigationKey): FragmentHost? {
    val primaryFragment = childFragmentManager.primaryNavigationFragment
    val activeContainerId = (primaryFragment?.view?.parent as? View)?.id

    val visibleContainers = getNavigationHandleViewModel().childContainers.filter {
        when (contextReference) {
            is FragmentActivity -> contextReference.findViewById<View>(it.containerId).isVisible
            is Fragment -> contextReference.requireView()
                .findViewById<View>(it.containerId).isVisible
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
            fragmentManager = childFragmentManager,
            accept = it::accept
        )
    } ?: parentContext()?.fragmentHostFor(key)
}

internal fun  Fragment.fragmentHostFrom(container: View): FragmentHost? {
    return getNavigationHandleViewModel()
        .navigationContext!!
        .parentContext()!!
        .getNavigationHandleViewModel()
        .childContainers
        .filter {
            container.id == it.containerId
        }
        .firstOrNull()
        ?.let {
            FragmentHost(
                containerId = it.containerId,
                fragmentManager = childFragmentManager,
                accept = it::accept
            )
        }
}