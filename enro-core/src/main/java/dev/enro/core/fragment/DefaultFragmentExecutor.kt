package dev.enro.core.fragment

import android.os.Bundle
import androidx.fragment.app.*
import dev.enro.core.*
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

        if (fromContext is FragmentContext && !fromContext.fragment.isAdded) return

        val instruction = args.instruction
        val fragmentActivity = fromContext.activity
        if (fragmentActivity !is FragmentActivity) {
            openFragmentAsActivity(fromContext, instruction.navigationDirection, instruction)
            return
        }

        when (instruction.navigationDirection) {
            NavigationDirection.ReplaceRoot -> {
                openFragmentAsActivity(fromContext, instruction.navigationDirection, instruction)
            }
            NavigationDirection.Present -> {
                val isDialog = DialogFragment::class.java.isAssignableFrom(args.navigator.contextType.java)
                when {
                    isDialog -> {
                        val fragment = createFragment(
                            fragmentActivity.supportFragmentManager,
                            navigator,
                            instruction,
                        ) as DialogFragment

                        fragment.showNow(
                            fragmentActivity.supportFragmentManager,
                            instruction.instructionId
                        )
                    }
                    else -> openFragmentAsActivity(fromContext, instruction.navigationDirection, instruction)
                }
            }
            NavigationDirection.Forward -> {
                instruction as OpenForwardInstruction

                val containerManager = args.fromContext.containerManager
                val host = containerManager.activeContainer?.takeIf { it.accept(args.key) }
                    ?: args.fromContext.containerManager.containers
                        .filterIsInstance<FragmentNavigationContainer>()
                        .firstOrNull { it.accept(args.key) }

                if (host == null) {
                    val parentContext = fromContext.parentContext()
                    if(parentContext == null) {
                        openFragmentAsActivity(fromContext, NavigationDirection.Present, args.instruction)
                    }
                    else {
                        parentContext.controller.open(
                            parentContext,
                            args.instruction
                        )
                    }
                    return
                }

                host.setBackstack(
                    host.backstackFlow.value.push(
                        instruction
                    )
                )
            }
        }
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
        instruction: AnyOpenInstruction
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
    navigationDirection: NavigationDirection,
    instruction: AnyOpenInstruction
) {
    instruction as NavigationInstruction.Open<NavigationDirection>
    fromContext.controller.open(
        fromContext,
        NavigationInstruction.Open.OpenInternal(
            navigationDirection = instruction.navigationDirection,
            navigationKey = SingleFragmentKey(instruction.internal.copy(
                navigationDirection = navigationDirection,
            ))
        )
    )
}
