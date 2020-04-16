package nav.enro.core.internal.executors

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import nav.enro.core.*
import nav.enro.core.internal.context.ActivityContext
import nav.enro.core.internal.context.FragmentContext
import nav.enro.core.internal.context.NavigationContext
import nav.enro.core.internal.context.ParentKey
import nav.enro.core.internal.context.navigationContext
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
        if (instruction.navigationDirection == NavigationDirection.REPLACE && fromContext is ActivityContext) {
            openAsSingleFragment(fromContext, instruction)
            return
        }

        val activity = when (fromContext) {
            is FragmentContext -> fromContext.fragment.requireActivity()
            is ActivityContext -> fromContext.activity
        }

        if(navigator.isDialog){
            val fragment = createFragment(
                activity.supportFragmentManager,
                fromContext,
                navigator,
                instruction
            ) as DialogFragment

            fragment.show(activity.supportFragmentManager, "")
            return
        }

        val host = fromContext.fragmentHostFor(navigator)
        if(host == null) {
            openAsSingleFragment(fromContext, instruction)
            return
        }

        host.fragmentManager.beginTransaction()
            .setCustomAnimations(activity.openEnterAnimation, activity.openExitAnimation)
            .replace(host.containerView, createFragment(
                host.fragmentManager,
                fromContext,
                navigator,
                instruction
            ))
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

    private fun createFragment(
        fragmentManager: FragmentManager,
        fromContext: NavigationContext<*>,
        navigator: Navigator<*>,
        instruction: NavigationInstruction.Open
    ) : Fragment {
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

        val fragment = fragmentManager.fragmentFactory.instantiate(
            navigator.contextType.java.classLoader!!,
            navigator.contextType.java.name
        )

        fragment.arguments = Bundle().apply {
            putParcelable(FragmentContext.ARG_PARENT_KEY, parentKey)
            putParcelable(NavigationContext.ARG_NAVIGATION_KEY, instruction.navigationKey)
            putParcelableArrayList(NavigationContext.ARG_CHILDREN, ArrayList(instruction.children))
        }

        return fragment
    }

    private fun openAsSingleFragment(
            fromContext: NavigationContext<*>,
            instruction: NavigationInstruction.Open
    ) {
        fromContext.controller.open(
            fromContext,
            NavigationInstruction.Open(
                instruction.navigationDirection,
                SingleFragmentKey(),
                listOf(instruction.navigationKey) + instruction.children
            )
        )
    }
}