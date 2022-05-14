package dev.enro.core.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.*
import dev.enro.core.*
import dev.enro.core.container.asPresentInstruction
import dev.enro.core.container.asPushInstruction
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

        val isReplace = args.instruction.navigationDirection is NavigationDirection.Replace
        val isDialog = DialogFragment::class.java.isAssignableFrom(args.navigator.contextType.java)
        val instruction = when(args.instruction.navigationDirection) {
            is NavigationDirection.Replace,
            is NavigationDirection.Forward -> when {
                isDialog -> args.instruction.asPresentInstruction()
                else -> args.instruction.asPushInstruction()
            }
            else -> args.instruction
        }
        val fragmentActivity = fromContext.activity
        if (fragmentActivity !is FragmentActivity) {
            openFragmentAsActivity(fromContext, instruction.navigationDirection, instruction)
            return
        }

        Log.e("open", ""+ "${args.fromContext} " +instruction.internal.toString())

        when (instruction.navigationDirection) {
            NavigationDirection.ReplaceRoot -> {
                openFragmentAsActivity(fromContext, instruction.navigationDirection, instruction)
            }
            NavigationDirection.Present -> {
                when {
                    isDialog -> openFragmentAsDialog(fromContext, navigator, instruction)
                    else -> openFragmentAsActivity(fromContext, instruction.navigationDirection, instruction)
                }
                if(isReplace) {
                    fromContext.getNavigationHandle().close()
                }
            }
            NavigationDirection.Push -> {
                val containerManager = args.fromContext.containerManager
                val host = containerManager.activeContainer?.takeIf { it.accept(args.key) }
                    ?: args.fromContext.containerManager.containers
                        .filterIsInstance<FragmentNavigationContainer>()
                        .firstOrNull { it.accept(args.key) }

                if (host == null) {
                    val parentContext = fromContext.parentContext()
                    if(parentContext == null) {
                        openFragmentAsActivity(fromContext, NavigationDirection.Present, instruction)
                        if(isReplace) {
                            fromContext.getNavigationHandle().close()
                        }
                    }
                    else {
                        parentContext.controller.open(
                            parentContext,
                            args.instruction
                        )
                    }
                    return
                }
                if(fromContext is ActivityContext && isReplace) {
                    openFragmentAsActivity(fromContext, NavigationDirection.Present, instruction)
                    fromContext.activity.finish()
                    return
                }

                host.setBackstack(
                    host.backstackFlow.value
                        .let {
                            if(isReplace) it.close() else it
                        }
                        .push(
                            instruction.asPushInstruction()
                        )
                )
            }
            else -> throw IllegalStateException()
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
    Log.e("open activity", ""+ " " +instruction.internal.toString())

    instruction as NavigationInstruction.Open<NavigationDirection>
    fromContext.controller.open(
        fromContext,
        NavigationInstruction.Open.OpenInternal(
            navigationDirection = instruction.navigationDirection,
            navigationKey = SingleFragmentKey(instruction.internal.copy(
                navigationDirection = navigationDirection,
            )),
        )
    )
}

private fun openFragmentAsDialog(
    fromContext: NavigationContext<out Any>,
    navigator: FragmentNavigator<*, *>,
    instruction: AnyOpenInstruction
) {
    val fragmentActivity = fromContext.activity as FragmentActivity
    val fragment = DefaultFragmentExecutor.createFragment(
        fragmentActivity.supportFragmentManager,
        navigator,
        instruction,
    ) as DialogFragment

    fragment.showNow(
        fragmentActivity.supportFragmentManager,
        instruction.instructionId
    )
}
