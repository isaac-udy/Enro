package dev.enro.core.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.*
import dev.enro.core.*
import dev.enro.core.fragment.internal.AbstractSingleFragmentActivity
import dev.enro.core.fragment.internal.SingleFragmentKey
import dev.enro.core.fragment.internal.fragmentHostFor

object DefaultFragmentExecutor : NavigationExecutor<Any, Fragment, NavigationKey>(
    fromType = Any::class,
    opensType = Fragment::class,
    keyType = NavigationKey::class
) {
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    override fun open(args: ExecutorArgs<out Any, out Fragment, out NavigationKey>) {
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

        if (!tryExecutePendingTransitions(navigator, fromContext, instruction)) return
        val fragment = createFragment(
            fromContext.childFragmentManager,
            navigator,
            instruction
        )

        if(fragment is DialogFragment) {
            if(fromContext.contextReference is DialogFragment) {
                if (instruction.navigationDirection == NavigationDirection.REPLACE) {
                    fromContext.contextReference.dismiss()
                }

                fragment.show(
                    fromContext.contextReference.parentFragmentManager,
                    instruction.instructionId
                )
            }
            else {
                fragment.show(fromContext.childFragmentManager, instruction.instructionId)
            }
            return
        }

        val host = fromContext.fragmentHostFor(instruction.navigationKey)
        if (host == null) {
            openFragmentAsActivity(fromContext, instruction)
            return
        }

        val activeFragment = host.fragmentManager.findFragmentById(host.containerId)
        activeFragment?.view?.let {
            ViewCompat.setZ(it, -1.0f)
        }

        val animations = animationsFor(fromContext, instruction)

        host.fragmentManager.commit {
            setCustomAnimations(animations.enter, animations.exit)

            if(fromContext.contextReference is DialogFragment && instruction.navigationDirection == NavigationDirection.REPLACE) {
                fromContext.contextReference.dismiss()
            }

            if(activeFragment != null
                && activeFragment.tag != null
                && activeFragment.tag == activeFragment.navigationContext.getNavigationHandleViewModel().id
                && activeFragment.tag == instruction.internal.parentInstruction?.instructionId
            ){
                detach(activeFragment)
            }

            replace(host.containerId, fragment, instruction.instructionId)
            setPrimaryNavigationFragment(fragment)
        }
    }

    override fun close(context: NavigationContext<out Fragment>) {
        if (context.contextReference is DialogFragment) {
            context.contextReference.dismiss()
            return
        }

        val previousFragment = context.getPreviousFragment()
        if (previousFragment == null && context.activity is AbstractSingleFragmentActivity) {
            context.controller.close(context.activity.navigationContext)
            return
        }

        val animations = animationsFor(context, NavigationInstruction.Close)
        // Checking for non-null context seems to be the best way to make sure parentFragmentManager will
        // not throw an IllegalStateException when there is no parent fragment manager
        val differentFragmentManagers = previousFragment?.context != null && previousFragment.parentFragmentManager != context.fragment.parentFragmentManager

        context.fragment.parentFragmentManager.commit {
            setCustomAnimations(animations.enter, animations.exit)
            remove(context.fragment)

            if (previousFragment != null && !differentFragmentManagers) {
                when {
                    previousFragment.isDetached -> attach(previousFragment)
                    !previousFragment.isAdded -> add(context.contextReference.getContainerId(), previousFragment)
                }
            }
            if(!differentFragmentManagers) setPrimaryNavigationFragment(previousFragment)
        }

        if(previousFragment != null && differentFragmentManagers) {
            previousFragment.parentFragmentManager.commit {
                setPrimaryNavigationFragment(previousFragment)
            }
        }
    }

    fun createFragment(
        fragmentManager: FragmentManager,
        navigator: Navigator<*, *>,
        instruction: NavigationInstruction.Open
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
        fromContext: NavigationContext<out Any>,
        instruction: NavigationInstruction.Open
    ): Boolean {
        try {
            fromContext.fragmentHostFor(instruction.navigationKey)?.fragmentManager?.executePendingTransactions()
            return true
        } catch (ex: IllegalStateException) {
            mainThreadHandler.post {
                when (fromContext) {
                    is ActivityContext -> fromContext.activity.getNavigationHandle().executeInstruction(
                        instruction
                    )
                    is FragmentContext -> {
                        if(!fromContext.fragment.isAdded) return@post
                        fromContext.fragment.getNavigationHandle().executeInstruction(
                            instruction
                        )
                    }
                }
            }
            return false
        }
    }

    private fun openFragmentAsActivity(
        fromContext: NavigationContext<out Any>,
        instruction: NavigationInstruction.Open
    ) {
        if(fromContext.contextReference is DialogFragment && instruction.navigationDirection == NavigationDirection.REPLACE) {
            // If we attempt to openFragmentAsActivity into a DialogFragment using the REPLACE direction,
            // the Activity hosting the DialogFragment will be closed/replaced
            // Instead, we close the fromContext's DialogFragment and call openFragmentAsActivity with the instruction changed to a forward direction
            openFragmentAsActivity(fromContext, instruction.internal.copy(navigationDirection = NavigationDirection.FORWARD))
            fromContext.contextReference.dismiss()
            return
        }

        fromContext.controller.open(
            fromContext,
            NavigationInstruction.Open.OpenInternal(
                navigationDirection = instruction.navigationDirection,
                navigationKey = SingleFragmentKey(instruction.internal.copy(
                    navigationDirection = NavigationDirection.FORWARD,
                    parentInstruction = null
                ))
            )
        )
    }
}

private fun NavigationContext<out Fragment>.getPreviousFragment(): Fragment? {
    val previouslyActiveFragment = getNavigationHandleViewModel().instruction.internal.previouslyActiveId
        ?.let { previouslyActiveId ->
            fragment.parentFragmentManager.fragments.firstOrNull {
                it.getNavigationHandle().id == previouslyActiveId && it.isVisible
            }
        }

    val containerView = contextReference.getContainerId()
    val parentInstruction = getNavigationHandleViewModel().instruction.internal.parentInstruction
    parentInstruction ?: return previouslyActiveFragment

    val previousNavigator = controller.navigatorForKeyType(parentInstruction.navigationKey::class)
    if(previousNavigator !is FragmentNavigator) return previouslyActiveFragment
    val previousHost = fragmentHostFor(parentInstruction.navigationKey)
    val previousFragment = previousHost?.fragmentManager?.findFragmentByTag(parentInstruction.instructionId)

    return when {
        previousFragment != null -> previousFragment
        previousHost?.containerId == containerView -> previousHost.fragmentManager.fragmentFactory
            .instantiate(
                previousNavigator.contextType.java.classLoader!!,
                previousNavigator.contextType.java.name
            )
            .apply {
                arguments = Bundle().addOpenInstruction(
                    parentInstruction.copy(
                        children = emptyList()
                    )
                )
            }
        else -> previousHost?.fragmentManager?.findFragmentById(previousHost.containerId)
    } ?: previouslyActiveFragment
}

private fun Fragment.getContainerId() = (requireView().parent as View).id