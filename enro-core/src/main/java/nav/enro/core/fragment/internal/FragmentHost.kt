package nav.enro.core.fragment.internal

import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import nav.enro.core.NavigationContext
import nav.enro.core.NavigationKey
import nav.enro.core.getNavigationHandleViewModel
import nav.enro.core.parentContext

internal class FragmentHost(
    internal val containerId: Int,
    internal val fragmentManager: FragmentManager
)

internal fun NavigationContext<*>.fragmentHostFor(key: NavigationKey): FragmentHost? {
    val primaryFragment = childFragmentManager.primaryNavigationFragment
    val activeContainerId = (primaryFragment?.view?.parent as? View)?.id

    val visibleContainers = getNavigationHandleViewModel().childContainers.filter {
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