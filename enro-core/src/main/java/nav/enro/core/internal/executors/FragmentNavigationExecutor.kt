package nav.enro.core.internal.executors

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import nav.enro.core.internal.context.ActivityContext
import nav.enro.core.internal.context.FragmentContext
import nav.enro.core.internal.context.NavigationContext
import nav.enro.core.internal.context.ParentKey
import nav.enro.core.internal.context.navigationContext
import nav.enro.core.FragmentNavigator
import nav.enro.core.NavigationDirection
import nav.enro.core.NavigationInstruction
import nav.enro.core.Navigator
import nav.enro.core.internal.*
import nav.enro.core.internal.SingleFragmentActivity
import nav.enro.core.internal.SingleFragmentKey
import nav.enro.core.internal.openEnterAnimation
import nav.enro.core.internal.openExitAnimation

internal class FragmentNavigationExecutor : NavigationExecutor {

    override fun open(
        navigator: Navigator<*>,
        fromContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open
    ) {
        navigator as FragmentNavigator

        if (instruction.navigationDirection == NavigationDirection.REPLACE_ROOT) {
            openAsSingleFragment(fromContext, instruction)
            return
        }

        val parentKey = when (fromContext) {
            is ActivityContext -> null
            is FragmentContext -> when (instruction.navigationDirection) {
                NavigationDirection.REPLACE_ROOT -> null
                NavigationDirection.FORWARD -> ParentKey(fromContext.key, fromContext.parentKey)
                NavigationDirection.REPLACE -> fromContext.parentKey?.let {
                    ParentKey(it.key, it.parent)
                }
            }
        }

        val host = fromContext.fragmentHostFor(instruction.navigationKey)
        if(host == null) {
            openAsSingleFragment(fromContext, instruction)
            return
        }

        val activity = when (fromContext) {
            is FragmentContext -> fromContext.fragment.requireActivity()
            is ActivityContext -> fromContext.activity
        }

        val fragment = host.fragmentManager.fragmentFactory.instantiate(
            navigator.contextType.java.classLoader!!,
            navigator.contextType.java.name
        )
        fragment.arguments = Bundle().apply {
            putParcelable(FragmentContext.ARG_PARENT_KEY, parentKey)
            putParcelable(NavigationContext.ARG_NAVIGATION_KEY, instruction.navigationKey)
            putParcelableArrayList(NavigationContext.ARG_CHILDREN, ArrayList(instruction.children))
        }

        if (fragment is DialogFragment) {
            fragment.show(host.fragmentManager, null)
            return
        }

        host.fragmentManager.beginTransaction()
            .setCustomAnimations(activity.openEnterAnimation, activity.openExitAnimation)
            .replace(host.containerView, fragment)
            .commitNow()
    }

    override fun close(context: NavigationContext<*>) {
        context as FragmentContext

        if (context.fragment is DialogFragment) {
            context.fragment.dismiss()
            return
        }

        val host = context.fragmentHost
        val parentKey = context.parentKey
        if (parentKey != null) {
            val previousNavigator = context.controller.navigatorFromKeyType(parentKey.key::class)
            previousNavigator as FragmentNavigator

            val previousFragment = host.fragmentManager.fragmentFactory.instantiate(
                previousNavigator.contextType.java.classLoader!!,
                previousNavigator.contextType.java.name
            )
            previousFragment.arguments = Bundle().apply {
                putParcelable(FragmentContext.ARG_PARENT_KEY, parentKey.parent)
                putParcelable(NavigationContext.ARG_NAVIGATION_KEY, parentKey.key)
            }
            host.fragmentManager.beginTransaction()
                .setCustomAnimations(
                    context.fragment.requireActivity().closeEnterAnimation,
                    context.fragment.requireActivity().closeExitAnimation
                )
                .replace(host.containerView, previousFragment)
                .commitNow()
        } else {
            when (val activity = context.fragment.requireActivity()) {
                is SingleFragmentActivity -> context.controller.close(activity.navigationContext)
                else -> host.fragmentManager.beginTransaction()
                    .setCustomAnimations(
                        activity.closeEnterAnimation,
                        activity.closeExitAnimation
                    )
                    .remove(
                        host.fragmentManager.findFragmentById(host.containerView) ?: return
                    )
                    .commitNow()
            }
        }
    }

    private fun openAsSingleFragment(
            fromContext: NavigationContext<*>,
            instruction: NavigationInstruction.Open
    ) {
        fromContext.controller.open(
            fromContext,
            NavigationInstruction.Open(
                instruction.navigationDirection,
                SingleFragmentKey(instruction.navigationKey),
                instruction.children
            )
        )
    }
}