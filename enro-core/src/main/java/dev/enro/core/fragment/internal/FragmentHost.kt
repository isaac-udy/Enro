package dev.enro.core.fragment.internal

import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import dev.enro.core.*
import dev.enro.core.getNavigationHandleViewModel
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import java.lang.IllegalStateException

internal class FragmentHost(
    internal val containerId: Int,
    internal val fragmentManager: FragmentManager,
    internal val accept: (NavigationKey) -> Boolean
)

internal fun NavigationContext<*>.fragmentHostFor(navigationInstruction: NavigationInstruction.Open): FragmentHost? {
    val targetContainer = navigationInstruction.getTargetContainer()
    if(targetContainer != null) {
        val target = getViewForId(targetContainer)
        return target?.let {
            return FragmentHost(
                containerId = targetContainer,
                fragmentManager = childFragmentManager,
                accept = { false }
            )
        } ?: parentContext()?.fragmentHostFor(navigationInstruction)
    }

    val key = navigationInstruction.navigationKey
    val primaryFragment = childFragmentManager.primaryNavigationFragment
    val activeContainerId = (primaryFragment?.view?.parent as? View)?.id

    val visibleContainers = getNavigationHandleViewModel().childContainers.filter {
        getViewForId(it.containerId)?.isVisible == true
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
    } ?: parentContext()?.fragmentHostFor(navigationInstruction)
}

internal fun NavigationContext<*>.getViewForId(id: Int): View? {
    return when (contextReference) {
        is FragmentActivity -> contextReference.findViewById<View>(id)
        is Fragment -> contextReference.requireView().findViewById<View>(id)
        else -> null
    }
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