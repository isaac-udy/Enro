package nav.enro.core.internal.context

import android.R
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import nav.enro.core.NavigationController
import nav.enro.core.NavigationKey
import nav.enro.core.navigationController
import kotlinx.android.parcel.Parcelize
import kotlin.reflect.KClass

internal sealed class NavigationContext<T : NavigationKey> {
    internal abstract val controller: NavigationController
    internal abstract val lifecycle: Lifecycle
    protected abstract val contextType: KClass<*>
    protected abstract val arguments: Bundle?

    internal val key: T by lazy {
        arguments?.getParcelable(ARG_NAVIGATION_KEY)
            ?: controller.navigatorFromContextType(contextType)?.defaultKey as? T
            ?: TODO()

    }

    internal val pendingKeys: List<NavigationKey> by lazy {
        arguments?.getParcelableArrayList<NavigationKey>(ARG_CHILDREN).orEmpty()
    }

    companion object {
        internal const val ARG_NAVIGATION_KEY = "nav.enro.core.ARG_NAVIGATION_KEY"
        internal const val ARG_CHILDREN = "nav.enro.core.ARG_CHILDREN"
    }
}

internal class ActivityContext<T : NavigationKey>(
    val activity: FragmentActivity
) : NavigationContext<T>() {
    override val controller get() = activity.application.navigationController
    override val lifecycle get() = activity.lifecycle
    override val arguments get() = activity.intent.extras
    override val contextType get() = activity::class

    val fragmentHost: FragmentHost = FragmentHost(
        R.id.content,
        activity.supportFragmentManager
    )
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

    internal val parentKey by lazy {
        arguments?.getParcelable<ParentKey>(ARG_PARENT_KEY)
    }

    internal val fragmentHost: FragmentHost = run {
        val parentContext: NavigationContext<*> = when {
            fragment.parentFragment != null -> fragment.requireParentFragment().navigationContext
            else -> fragment.requireActivity().navigationContext
        }

        return@run when (parentContext) {
            is FragmentContext -> parentContext.childFragmentHost!!
            is ActivityContext -> parentContext.fragmentHost
        }
    }

    internal val childFragmentHost: FragmentHost? = null

    companion object {
        internal const val ARG_PARENT_KEY = "nav.enro.core.ARG_PARENT_KEY"
    }
}