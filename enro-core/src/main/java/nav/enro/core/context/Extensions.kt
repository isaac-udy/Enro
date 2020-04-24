package nav.enro.core.context

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import nav.enro.core.NavigationKey
import nav.enro.core.addOpenInstruction
import nav.enro.core.internal.handle.NavigationHandleViewModel
import nav.enro.core.navigator.ActivityNavigator
import nav.enro.core.navigator.FragmentHost
import nav.enro.core.navigator.FragmentNavigator
import java.lang.IllegalStateException
import kotlin.reflect.KClass

val NavigationContext<out FragmentActivity, *>.activity get() = contextReference
val NavigationContext<out Fragment, *>.fragment get() = contextReference

val NavigationContext<*, *>.parentActivity: FragmentActivity get() = when (contextReference) {
    is FragmentActivity -> contextReference
    is Fragment -> contextReference.requireActivity()
    else -> throw IllegalStateException()
}

internal fun NavigationContext<*, *>.fragmentHostFor(fragmentToHost: KClass<out Fragment>): FragmentHost? {
    val primaryFragment = childFragmentManager.primaryNavigationFragment
    val activeContainerId = (primaryFragment?.view?.parent as? View)?.id
    val primaryDefinition = navigator.fragmentHosts.firstOrNull {
        it.containerView == activeContainerId && it.accepts(fragmentToHost)
    }
    val definition = primaryDefinition
        ?: navigator.fragmentHosts.firstOrNull { it.accepts(fragmentToHost) }

    return definition?.createFragmentHost(childFragmentManager)
        ?: parentContext()?.fragmentHostFor(fragmentToHost)
}

fun NavigationContext<out Fragment, *>.getParentFragment(): Fragment? {
    val containerView = (contextReference.requireView().parent as View).id
    val parentInstruction = parentInstruction
    parentInstruction ?: return null

    val previousNavigator = controller.navigatorForKeyType(parentInstruction.navigationKey::class)
    if(previousNavigator is ActivityNavigator) return null
    previousNavigator as FragmentNavigator<*, *>
    val previousHost = fragmentHostFor(previousNavigator.contextType)

    return when (previousHost?.containerView) {
        containerView -> previousHost.fragmentManager.fragmentFactory
            .instantiate(
                previousNavigator.contextType.java.classLoader!!,
                previousNavigator.contextType.java.name
            )
            .apply {
                arguments = Bundle().addOpenInstruction(parentInstruction)
            }
        else -> previousHost?.fragmentManager?.findFragmentById(previousHost.containerView)
    }
}

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
    val childContext = primaryNavigationFragment.navigationContext
    return childContext.leafContext()
}

@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
internal val <T: FragmentActivity> T.navigationContext: ActivityContext<T, Nothing>
    get() = viewModels<NavigationHandleViewModel<Nothing>>().value.navigationContext as ActivityContext<T, Nothing>

@Suppress("UNCHECKED_CAST") // Higher level logic dictates this cast will pass
internal val <T: Fragment> T.navigationContext: FragmentContext<T, Nothing>
    get() = viewModels<NavigationHandleViewModel<Nothing>>().value.navigationContext as FragmentContext<T, Nothing>