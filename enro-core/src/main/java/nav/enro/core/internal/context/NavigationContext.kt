package nav.enro.core.internal.context

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

sealed class NavigationContext<ContextType : Any, T : NavigationKey>(
    val contextReference: ContextType
) {
    abstract val controller: NavigationController
    abstract val lifecycle: Lifecycle
    abstract val childFragmentManager: FragmentManager
    protected abstract val arguments: Bundle?

    val instruction by lazy { arguments?.readOpenInstruction<T>() }

    val key: T by lazy {
        instruction?.navigationKey
            ?: controller.navigatorForContextType(contextReference::class)?.defaultKey as? T
            ?: throw IllegalStateException("Navigation Context's bound arguments did not contain a NavigationKey!")
    }

    internal open val navigator: Navigator<ContextType, T> by lazy {
        controller.navigatorForKeyType(key::class) as Navigator<ContextType, T>
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
        val primaryDefinition = navigator.fragmentHosts.firstOrNull {
            it.containerView == activeContainerId && it.accepts(fragmentToHost)
        }
        val definition = primaryDefinition
            ?: navigator.fragmentHosts.firstOrNull { it.accepts(fragmentToHost) }

        return definition?.createFragmentHost(childFragmentManager)
            ?: parentContext()?.fragmentHostFor(fragmentToHost)
    }
}

internal class ActivityContext<ContextType : FragmentActivity, T : NavigationKey>(
    contextReference: ContextType
) : NavigationContext<ContextType, T>(contextReference) {
    override val controller get() = contextReference.application.navigationController
    override val lifecycle get() = contextReference.lifecycle
    override val arguments get() = contextReference.intent.extras
    override val navigator get() = super.navigator as ActivityNavigator<ContextType, T>
    override val childFragmentManager get() = contextReference.supportFragmentManager
}

internal class FragmentContext<ContextType : Fragment, T : NavigationKey>(
    contextReference: ContextType
) : NavigationContext<ContextType, T>(contextReference) {
    override val controller get() = contextReference.requireActivity().application.navigationController
    override val lifecycle get() = contextReference.lifecycle
    override val arguments get() = contextReference.arguments
    override val navigator get() = super.navigator as FragmentNavigator<ContextType, T>
    override val childFragmentManager get() = contextReference.childFragmentManager
}

val NavigationContext<out FragmentActivity, *>.activity get() = contextReference
val NavigationContext<out Fragment, *>.fragment get() = contextReference

fun NavigationContext<*, *>.requireActivity(): FragmentActivity = when (contextReference) {
    is FragmentActivity -> contextReference
    is Fragment -> contextReference.requireActivity()
    else -> throw IllegalStateException()
}