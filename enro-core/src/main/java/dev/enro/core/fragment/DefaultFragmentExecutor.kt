package dev.enro.core.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.*
import dev.enro.core.*
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.fragment.container.FragmentNavigationContainer
import dev.enro.core.fragment.internal.SingleFragmentKey

private const val PREVIOUS_FRAGMENT_IN_CONTAINER = "dev.enro.core.fragment.DefaultFragmentExecutor.PREVIOUS_FRAGMENT_IN_CONTAINER"

object DefaultFragmentExecutor : NavigationExecutor<Any, Fragment, NavigationKey>(
    fromType = Any::class,
    opensType = Fragment::class,
    keyType = NavigationKey::class
) {
    override fun open(args: ExecutorArgs<out Any, out Fragment, out NavigationKey>) {
        val fromContext = args.fromContext
        val navigator = args.navigator as FragmentNavigator
        val instruction = args.instruction

        val containerManager = args.fromContext.containerManager
        val host = containerManager.activeContainer?.takeIf { it.accept(args.key) }
            ?: args.fromContext.containerManager.containers
                .filterIsInstance<FragmentNavigationContainer>()
                .firstOrNull { it.accept(args.key) }

        if (fromContext is FragmentContext && !fromContext.fragment.isAdded) return
        if (host == null) {
            val parentContext = fromContext.parentContext()
            if(parentContext == null) {
                openFragmentAsActivity(fromContext, instruction)
            }
            else {
                open(
                    ExecutorArgs(
                        parentContext,
                        navigator,
                        instruction.navigationKey,
                        instruction
                    )
                )
            }
            return
        }

        if (instruction.navigationDirection == NavigationDirection.REPLACE_ROOT) {
            openFragmentAsActivity(fromContext, instruction)
            return
        }

        val fragmentActivity = fromContext.activity
        if (fragmentActivity !is FragmentActivity) {
            openFragmentAsActivity(fromContext, instruction)
            return
        }

        if (instruction.navigationDirection == NavigationDirection.REPLACE && fromContext.contextReference is FragmentActivity) {
            openFragmentAsActivity(fromContext, instruction)
            return
        }

        if(instruction.navigationDirection == NavigationDirection.REPLACE && fromContext.contextReference is ComposableDestination) {
            TODO()
//            fromContext.contextReference.contextReference.requireParentContainer().close()
        }

        val isDialog = DialogFragment::class.java.isAssignableFrom(args.navigator.contextType.java)

        if(isDialog) {
            val fragment = createFragment(
                fragmentActivity.supportFragmentManager,
                navigator,
                instruction,
            ) as DialogFragment

            if(fromContext.contextReference is DialogFragment) {
                if (instruction.navigationDirection == NavigationDirection.REPLACE) {
                    fromContext.contextReference.dismiss()
                }

                fragment.show(
                    fragmentActivity.supportFragmentManager,
                    instruction.instructionId
                )
            }
            else {
                fragment.show(fragmentActivity.supportFragmentManager, instruction.instructionId)
            }
            return
        }

        host.setBackstack(
            host.backstackFlow.value.push(
                args.instruction,
                args.fromContext.containerManager.activeContainer?.id
            )
        )
    }


    override fun close(context: NavigationContext<out Fragment>) {
        val container = context.parentContext()?.containerManager?.containers?.firstOrNull { it.activeContext == context }
        if(container == null) {
            context.contextReference.parentFragmentManager.commitNow {
                remove(context.contextReference)
            }
            return
        }

        container.setBackstack(
            container.backstackFlow.value.close(
                context.getNavigationHandle().id
            )
        )
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
