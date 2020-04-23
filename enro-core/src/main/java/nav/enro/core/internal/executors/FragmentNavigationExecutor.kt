package nav.enro.core.internal.executors

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewParent
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import nav.enro.core.*
import nav.enro.core.internal.*
import nav.enro.core.internal.SingleFragmentActivity
import nav.enro.core.internal.SingleFragmentKey
import nav.enro.core.internal.context.*
import nav.enro.core.internal.context.ActivityContext
import nav.enro.core.internal.context.FragmentContext
import nav.enro.core.internal.context.NavigationContext
import nav.enro.core.internal.context.navigationContext
import nav.enro.core.internal.executors.override.NavigationExecutorOverride
import nav.enro.core.internal.executors.override.PendingNavigationOverride
import nav.enro.core.internal.executors.override.activityToFragmentOverride
import nav.enro.core.internal.executors.override.fragmentToFragmentOverride
import nav.enro.core.internal.openEnterAnimation
import nav.enro.core.internal.openExitAnimation
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

internal class FragmentNavigationExecutor : NavigationExecutor() {
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    val defaultActivityToFragment = activityToFragmentOverride<FragmentActivity, Fragment>(
        launch = { from, instruction, to ->

            if (instruction.navigationDirection == NavigationDirection.REPLACE_ROOT
                || instruction.navigationDirection == NavigationDirection.REPLACE) {
                openAsSingleFragment(from.navigationContext, instruction)
                return@activityToFragmentOverride
            }

            if(to is DialogFragment) {
                to.show(from.supportFragmentManager, "")
                return@activityToFragmentOverride
            }

            val host = from.navigationContext.fragmentHostFor(to::class)
            if (host == null) {
                openAsSingleFragment(from.navigationContext, instruction)
                return@activityToFragmentOverride
            }

            host.fragmentManager.beginTransaction()
                .setCustomAnimations(
                    from.openEnterAnimation,
                    from.openExitAnimation
                )
                .replace(host.containerView, to)
                .setPrimaryNavigationFragment(to)
                .commitNow()
        },
        close = { from, to ->
            when (from) {
                is SingleFragmentActivity -> from.navigationContext.controller.close(from.navigationContext)
                else -> to.parentFragmentManager.beginTransaction()
                    .setPrimaryNavigationFragment(null)
                    .setCustomAnimations(
                        from.closeEnterAnimation,
                        from.closeExitAnimation
                    )
                    .remove(to)
                    .commitNow()
            }
        }
    )

    val defaultFragmentToFragment = fragmentToFragmentOverride<Fragment, Fragment>(
        launch = { from, instruction, to ->
            if (instruction.navigationDirection == NavigationDirection.REPLACE_ROOT) {
                openAsSingleFragment(from.navigationContext, instruction)
                return@fragmentToFragmentOverride
            }

            if(to is DialogFragment) {
                to.show(from.childFragmentManager, "")
                return@fragmentToFragmentOverride
            }

            val host = from.navigationContext.fragmentHostFor(to::class)
            if (host == null) {
                openAsSingleFragment(from.navigationContext, instruction)
                return@fragmentToFragmentOverride
            }

            val activeFragment = host.fragmentManager.findFragmentById(host.containerView)
            activeFragment?.view?.z = -1.0f

            host.fragmentManager.beginTransaction()
                .setCustomAnimations(
                    from.requireActivity().openEnterAnimation,
                    from.requireActivity().openExitAnimation
                )
                .replace(host.containerView, to)
                .setPrimaryNavigationFragment(to)
                .commitNow()
        },
        close = { from, to ->
            val container = (to.requireView().parent as View).id

            val transaction = to.parentFragmentManager
                .beginTransaction()
                .setCustomAnimations(
                    to.requireActivity().closeEnterAnimation,
                    to.requireActivity().closeExitAnimation
                )

            if (!from.isAdded) {
                transaction.replace(container, from)
            } else {
                transaction.remove(to)
            }
            transaction.setPrimaryNavigationFragment(from)
            transaction.commitNow()
        }
    )

    override fun open(
        navigator: Navigator<*>,
        fromContext: NavigationContext<out Any, *>,
        instruction: NavigationInstruction.Open<*>
    ) {
        navigator as FragmentNavigator
        if (!tryExecutePendingTransitions(navigator, fromContext, instruction)) return

        val override = fromContext.controller.pendingOverrideFor(
            from = fromContext.contextReference,
            toType = navigator.contextType
        ) ?: when(fromContext.contextReference) {
            is Fragment -> PendingNavigationOverride(fromContext.contextReference, defaultFragmentToFragment)
            is FragmentActivity -> PendingNavigationOverride(fromContext.contextReference, defaultActivityToFragment)
            else -> throw IllegalArgumentException("Unknown from context!")
        }

        override.launchFragment(
            instruction,
            createFragment(
                fromContext.activityFromContext.supportFragmentManager,
                fromContext,
                navigator,
                instruction
            )
        )
    }

    override fun close(context: NavigationContext<out Any, *>) {
        context as FragmentContext<out Fragment, *>

        if (context.fragment is DialogFragment) {
            context.fragment.dismiss()
            return
        }

        val containerView = (context.fragment.requireView().parent as View).id
        val parentInstruction = context.parentInstruction

        val previousFragment = run {
            parentInstruction ?: return@run null

            val previousNavigator =
                context.controller.navigatorForKeyType(parentInstruction.navigationKey::class)
            previousNavigator as FragmentNavigator
            val previousHost = context.fragmentHostFor(previousNavigator.contextType)

            return@run when (previousHost?.containerView) {
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

        if (previousFragment == null) {
            val activity = context.fragment.requireActivity()
            val override = context.controller.overrideFor(
                fromType = activity::class,
                toType = context.fragment::class
            ) ?: defaultActivityToFragment

            override as NavigationExecutorOverride<Any, Any>
            override.closeFragment(context.fragment.requireActivity(), context.fragment)
            return
        } else {
            val override = context.controller.overrideFor(
                fromType = previousFragment::class,
                toType = context.fragment::class
            ) ?: defaultFragmentToFragment

            override as NavigationExecutorOverride<Any, Any>
            override.closeFragment(previousFragment, context.fragment)
        }
    }

    private fun createFragment(
        fragmentManager: FragmentManager,
        fromContext: NavigationContext<out Any, *>,
        navigator: Navigator<*>,
        instruction: NavigationInstruction.Open<*>
    ): Fragment {
        val parentInstruction = when (fromContext) {
            is ActivityContext -> null
            is FragmentContext -> fromContext.instruction
        }
        val fragment = fragmentManager.fragmentFactory.instantiate(
            navigator.contextType.java.classLoader!!,
            navigator.contextType.java.name
        )

        fragment.arguments = Bundle()
            .addOpenInstruction(instruction.copy(parentInstruction = parentInstruction))

        return fragment
    }

    private fun tryExecutePendingTransitions(
        navigator: FragmentNavigator<*>,
        fromContext: NavigationContext<out Any, *>,
        instruction: NavigationInstruction.Open<*>
    ): Boolean {
        try {
            fromContext.fragmentHostFor(navigator.contextType)?.fragmentManager?.executePendingTransactions()
            return true
        } catch (ex: IllegalStateException) {
            mainThreadHandler.post {
                when (fromContext) {
                    is ActivityContext<out FragmentActivity, *> -> fromContext.activity.navigationHandle<Nothing>().value.execute(
                        instruction
                    )
                    is FragmentContext<out Fragment, *> -> fromContext.fragment.navigationHandle<Nothing>().value.execute(
                        instruction
                    )
                }
            }
            return false
        }
    }

    private fun openAsSingleFragment(
        fromContext: NavigationContext<out Any, *>,
        instruction: NavigationInstruction.Open<*>
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