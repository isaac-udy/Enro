package nav.enro.core.executors

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
            fragment.show(fromContext.childFragmentManager, instruction.id)
            return
        }

        val host = fromContext.fragmentHostFor(instruction.navigationKey)
        if (host == null) {
            openFragmentAsActivity(fromContext, instruction)
            return
        }

        val activeFragment = host.fragmentManager.findFragmentById(host.containerId)
        activeFragment?.view?.z = -1.0f

        val animations = navigator.animationsFor(fromContext.activity.theme, instruction)

        host.fragmentManager.commitNow {
            setCustomAnimations(animations.enter, animations.exit)
            replace(host.containerId, fragment)
            setPrimaryNavigationFragment(fragment)
        }
    }

    override fun close(context: NavigationContext<out Fragment, out NavigationKey>) {
        if (context.contextReference is DialogFragment) {
            context.contextReference.dismiss()
            return
        }

        val previousFragment = context.getParentFragment()
        if (previousFragment == null && context.activity is SingleFragmentActivity) {
            context.controller.close(context.activity.navigationContext)
            return
        }

        val animations = context.navigator.animationsFor(context.activity.theme, NavigationInstruction.Close)

        context.fragment.parentFragmentManager.commitNow {
            setCustomAnimations(animations.enter, animations.exit)

            if (previousFragment != null && !previousFragment.isAdded) {
                replace(context.fragment.getContainerId(), previousFragment)
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
            fromContext.fragmentHostFor(instruction.navigationKey)?.fragmentManager?.executePendingTransactions()
            return true
        } catch (ex: IllegalStateException) {
            mainThreadHandler.post {
                when (fromContext) {
                    is ActivityContext -> fromContext.activity.getNavigationHandle<Nothing>().executeInstruction(
                        instruction
                    )
                    is FragmentContext -> fromContext.fragment.getNavigationHandle<Nothing>().executeInstruction(
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
                SingleFragmentKey(instruction.copy(
                    navigationDirection = NavigationDirection.FORWARD,
                    parentInstruction = null
                ))
            )
        )
    }
}

fun NavigationContext<out Fragment, *>.getParentFragment(): Fragment? {
    val containerView = contextReference.getContainerId()
    val parentInstruction = parentInstruction
    parentInstruction ?: return null

    val previousNavigator = controller.navigatorForKeyType(parentInstruction.navigationKey::class)
    if(previousNavigator is ActivityNavigator) return null
    previousNavigator as FragmentNavigator<*, *>
    val previousHost = fragmentHostFor(parentInstruction.navigationKey)

    return when (previousHost?.containerId) {
        containerView -> previousHost.fragmentManager.fragmentFactory
            .instantiate(
                previousNavigator.contextType.java.classLoader!!,
                previousNavigator.contextType.java.name
            )
            .apply {
                arguments = Bundle().addOpenInstruction(parentInstruction?.let {
                    it.copy(
                        children = emptyList()
                    )
                })
            }
        else -> previousHost?.fragmentManager?.findFragmentById(previousHost.containerId)
    }
}
