package nav.enro.core.internal.context

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import nav.enro.core.*
import nav.enro.core.navigationController
import java.lang.IllegalStateException
import kotlin.reflect.KClass

sealed class NavigationContext<ContextType: Any, T : NavigationKey>(
    internal val contextReference: ContextType
) {
    internal abstract val controller: NavigationController
    internal abstract val lifecycle: Lifecycle
    internal abstract val childFragmentManager: FragmentManager
    internal abstract val contextType: KClass<*>
    protected abstract val arguments: Bundle?

    internal val instruction by lazy { arguments?.readOpenInstruction<T>() }

    internal val key: T by lazy {
        instruction?.navigationKey
            ?: controller.navigatorForContextType(contextType)?.defaultKey as? T
            ?: throw IllegalStateException("Navigation Context's bound arguments did not contain a NavigationKey!")
    }

    internal open val navigator: Navigator<T> by lazy {
        controller.navigatorForKeyType(key::class) as Navigator<T>
    }

    internal val pendingKeys: List<NavigationKey> by lazy {
        instruction?.children.orEmpty()
    }

    internal val parentInstruction by lazy {
        instruction?.parentInstruction
    }

    internal fun fragmentHostFor(fragmentToHost: KClass<out Fragment>): FragmentHost? {
        val primaryFragment = childFragmentManager.primaryNavigationFragment
        val activeContainerId = (primaryFragment?.view?.parent as? View)?.id
        val primaryDefinition = navigator.fragmentHosts.firstOrNull { it.containerView == activeContainerId && it.accepts(fragmentToHost) }
        val definition = primaryDefinition ?: navigator.fragmentHosts.firstOrNull { it.accepts(fragmentToHost) }
        if (definition == null) {
            val parentContext = parentContext()
            if (parentContext == this) return null
            return parentContext.fragmentHostFor(fragmentToHost)
        }
        return definition.createFragmentHost(childFragmentManager)
    }
}

internal class ActivityContext<ContextType: FragmentActivity, T : NavigationKey>(
    val activity: ContextType
) : NavigationContext<ContextType, T>(activity) {
    override val controller get() = activity.application.navigationController
    override val lifecycle get() = activity.lifecycle
    override val arguments get() = activity.intent.extras
    override val contextType get() = activity::class
    override val navigator get() = super.navigator as ActivityNavigator<T>
    override val childFragmentManager get() = contextReference.supportFragmentManager
}

internal class FragmentContext<ContextType: Fragment, T : NavigationKey>(
    val fragment: ContextType
) : NavigationContext<ContextType, T>(fragment) {
    override val controller get() = fragment.requireActivity().application.navigationController
    override val lifecycle get() = fragment.lifecycle
    override val arguments get() = fragment.arguments
    override val contextType get() = fragment::class
    override val navigator get() = super.navigator as FragmentNavigator<T>
    override val childFragmentManager get() = fragment.childFragmentManager
}

internal val NavigationContext<*, *>.activityFromContext: FragmentActivity
    get() = when (this) {
        is ActivityContext<out FragmentActivity, *> -> activity
        is FragmentContext<out Fragment, *> -> fragment.requireActivity()
    }