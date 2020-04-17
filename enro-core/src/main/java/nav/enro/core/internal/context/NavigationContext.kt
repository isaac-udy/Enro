package nav.enro.core.internal.context

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import kotlinx.android.parcel.Parcelize
import nav.enro.core.*
import nav.enro.core.navigationController
import kotlin.reflect.KClass

internal sealed class NavigationContext<T : NavigationKey> {
    internal abstract val controller: NavigationController
    internal abstract val lifecycle: Lifecycle
    protected abstract val contextType: KClass<*>
    protected abstract val arguments: Bundle?

    internal val instruction by lazy { arguments?.readOpenInstruction() }

    internal val key: T by lazy {
        instruction?.navigationKey as? T
            ?: controller.navigatorFromContextType(contextType)?.defaultKey as? T
            ?: TODO()
    }

    internal open val navigator: Navigator<T> by lazy {
        controller.navigatorFromKeyType(key::class) as Navigator<T>
    }

    internal val pendingKeys: List<NavigationKey> by lazy {
        instruction?.children.orEmpty()
    }

    internal val parentInstruction by lazy {
        instruction?.parentInstruction
    }

    internal val activeFragmentHost: FragmentHost? get() {
        val definition = navigator.fragmentHosts.firstOrNull() ?: return null
        return when (this) {
            is ActivityContext -> definition.createFragmentHost(activity)
            is FragmentContext -> definition.createFragmentHost(fragment)
        }
    }

    internal fun fragmentHostFor(navigatorToHost: Navigator<*>): FragmentHost? {
        val definition = navigator.fragmentHosts.firstOrNull { it.accepts(navigatorToHost) }
        if(definition == null) {
            val parentContext = parentContext()
            if(parentContext == this) return null
            return parentContext.fragmentHostFor(navigatorToHost)
        }
        return when(this) {
            is ActivityContext -> definition.createFragmentHost(activity)
            is FragmentContext -> definition.createFragmentHost(fragment)
        }
    }
}

internal class ActivityContext<T : NavigationKey>(
    val activity: FragmentActivity
) : NavigationContext<T>() {
    override val controller get() = activity.application.navigationController
    override val lifecycle get() = activity.lifecycle
    override val arguments get() = activity.intent.extras
    override val contextType get() = activity::class
    override val navigator get() = super.navigator as ActivityNavigator<T>
}


@Parcelize
internal data class ParentKey(
    val key: NavigationKey,
    val parent: ParentKey? = null
) : Parcelable

internal class FragmentContext<T : NavigationKey>(
    val fragment: androidx.fragment.app.Fragment
) : NavigationContext<T>() {
    override val controller  get() = fragment.requireActivity().application.navigationController
    override val lifecycle get() = fragment.lifecycle
    override val arguments  get() = fragment.arguments
    override val contextType get() = fragment::class
    override val navigator get() = super.navigator as FragmentNavigator<T>

    internal val fragmentHost: FragmentHost = run {
        val parentContext: NavigationContext<*> = when {
            fragment.parentFragment != null -> fragment.requireParentFragment().navigationContext
            else -> fragment.requireActivity().navigationContext
        }

        return@run parentContext.fragmentHostFor(navigator) ?: FragmentHost(0, fragment.parentFragmentManager)
    }
}