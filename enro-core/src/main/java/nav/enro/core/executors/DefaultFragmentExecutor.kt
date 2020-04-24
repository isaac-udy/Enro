package nav.enro.core.executors

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.*
import nav.enro.core.*
import nav.enro.core.context.*
import nav.enro.core.context.ActivityContext
import nav.enro.core.context.FragmentContext
import nav.enro.core.internal.*
import nav.enro.core.navigator.*

object DefaultFragmentExecutor : NavigationExecutor<Any, Fragment, NavigationKey>(
    fromType = Any::class,
    opensType = Fragment::class,
    keyType = NavigationKey::class
) {
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    override fun open(args: ExecutorArgs<Any, Fragment, NavigationKey>) {
        val fromContext = args.fromContext
        val navigator = args.navigator
        val instruction = args.instruction

        navigator as FragmentNavigator<*, *>
        if (instruction.navigationDirection == NavigationDirection.REPLACE_ROOT) {
            openFragmentAsActivity(fromContext, instruction)
            return
        }
        if (instruction.navigationDirection == NavigationDirection.REPLACE && fromContext.contextReference is FragmentActivity) {
            openFragmentAsActivity(fromContext, instruction)
            return
        }

        val host = fromContext.fragmentHostFor(navigator.contextType)
        if (host == null) {
            openFragmentAsActivity(fromContext, instruction)
            return
        }

        if (!tryExecutePendingTransitions(navigator, fromContext, instruction)) return
        val fragment = createFragment(
            fromContext.childFragmentManager,
            navigator,
            instruction
        )

        if(fragment is DialogFragment) {
            fragment.show(fromContext.childFragmentManager, instruction.id)
            return
        }

        val activeFragment = host.fragmentManager.findFragmentById(host.containerView)
        activeFragment?.view?.z = -1.0f

        val animations = navigator.animations
            .toResource(fromContext.parentActivity)
            .animationsForOpen(instruction.navigationDirection)

        host.fragmentManager.beginTransaction()
            .setCustomAnimations(animations.first, animations.second)
            .replace(host.containerView, fragment)
            .setPrimaryNavigationFragment(fragment)
            .commitNow()
    }

    override fun close(context: NavigationContext<out Fragment, out NavigationKey>) {
        if (context.contextReference is DialogFragment) {
            context.contextReference.dismiss()
            return
        }

        val previousFragment = context.getParentFragment()
        if (previousFragment == null && context.parentActivity is SingleFragmentActivity) {
            context.controller.close(context.parentActivity.navigationContext)
            return
        }

        val animations = context.navigator.animations
            .toResource(context.parentActivity)
            .animationsForClose()

        context.fragment.parentFragmentManager.commitNow {
            setCustomAnimations(animations.first, animations.second)

            if (previousFragment != null && !previousFragment.isAdded) {
                replace((context.fragment.requireView().parent as View).id, previousFragment)
            } else {
                remove(context.fragment)
            }
            setPrimaryNavigationFragment(previousFragment)
        }
    }

    fun createFragment(
        fragmentManager: FragmentManager,
        navigator: Navigator<*, *>,
        instruction: NavigationInstruction.Open<*>
    ): Fragment {
        val fragment = fragmentManager.fragmentFactory.instantiate(
            navigator.contextType.java.classLoader!!,
            navigator.contextType.java.name
        )

        fragment.arguments = Bundle()
            .addOpenInstruction(instruction)

        return fragment
    }

    private fun tryExecutePendingTransitions(
        navigator: FragmentNavigator<*, *>,
        fromContext: NavigationContext<out Any, *>,
        instruction: NavigationInstruction.Open<*>
    ): Boolean {
        try {
            fromContext.fragmentHostFor(navigator.contextType)?.fragmentManager?.executePendingTransactions()
            return true
        } catch (ex: IllegalStateException) {
            mainThreadHandler.post {
                when (fromContext) {
                    is ActivityContext -> fromContext.activity.navigationHandle<Nothing>().value.execute(
                        instruction
                    )
                    is FragmentContext -> fromContext.fragment.navigationHandle<Nothing>().value.execute(
                        instruction
                    )
                }
            }
            return false
        }
    }

    fun openFragmentAsActivity(
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