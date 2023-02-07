package dev.enro.core.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.*
import androidx.lifecycle.lifecycleScope
import dev.enro.core.*
import dev.enro.core.container.*
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.ExecuteOpenInstruction
import dev.enro.core.controller.usecase.HostInstructionAs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

public object DefaultFragmentExecutor : NavigationExecutor<Any, Fragment, NavigationKey>(
    fromType = Any::class,
    opensType = Fragment::class,
    keyType = NavigationKey::class
) {
    override fun open(args: ExecutorArgs<out Any, out Fragment, out NavigationKey>) {
        val fromContext = args.fromContext

        if (fromContext is FragmentContext && !fromContext.fragment.isAdded) return

        val isReplace = args.instruction.navigationDirection is NavigationDirection.Replace
        val isDialog =
            DialogFragment::class.java.isAssignableFrom(args.binding.destinationType.java)
        val instruction = when (args.instruction.navigationDirection) {
            is NavigationDirection.Replace,
            is NavigationDirection.Forward -> when {
                isDialog -> args.instruction.asPresentInstruction()
                else -> args.instruction.asPushInstruction()
            }
            else -> args.instruction
        }
        val fragmentActivity = fromContext.activity
        if (fragmentActivity !is FragmentActivity) {
            EnroException.LegacyNavigationDirectionUsedInStrictMode.logForStrictMode(fromContext.controller, args)
            openFragmentAsActivity(fromContext, instruction.navigationDirection, instruction)
            return
        }

        when (instruction.navigationDirection) {
            NavigationDirection.ReplaceRoot -> {
                openFragmentAsActivity(fromContext, instruction.navigationDirection, instruction)
            }
            NavigationDirection.Present,
            NavigationDirection.Push -> {
                val containerManager = args.fromContext.containerManager
                val host = containerManager.activeContainer?.takeIf {
                    it.isVisible && it.accept(instruction)
                } ?: args.fromContext.containerManager.containers
                        .filter { it.isVisible }
                        .firstOrNull { it.accept(instruction) }

                if (host == null) {
                    val parentContext = fromContext.parentContext()
                    if(parentContext == null) {
                        EnroException.MissingContainerForPushInstruction.logForStrictMode(
                            fromContext.controller,
                            args
                        )

                        if(instruction.navigationDirection == NavigationDirection.Present) {
                            openFragmentAsActivity(
                                fromContext,
                                NavigationDirection.Present,
                                instruction
                            )
                        }
                        else {
                            open(
                                ExecutorArgs(
                                    fromContext = fromContext,
                                    binding = args.binding,
                                    key = args.key,
                                    instruction = args.instruction.internal.copy(
                                        navigationDirection = NavigationDirection.Present
                                    )
                                )
                            )
                        }
                        if(isReplace) {
                            fromContext.getNavigationHandle().close()
                        }
                    }
                    else if(fromContext is ActivityContext && isReplace) {
                        openFragmentAsActivity(fromContext, NavigationDirection.Present, instruction)
                        fromContext.activity.finish()
                    }
                    else {
                        open(
                            ExecutorArgs(
                                fromContext = parentContext,
                                binding = args.binding,
                                key = args.key,
                                instruction = args.instruction
                            )
                        )
                    }
                    return
                }

                if(fromContext is ActivityContext && isReplace) {
                    openFragmentAsActivity(fromContext, NavigationDirection.Present, instruction)
                    fromContext.activity.finish()
                }

                EnroException.LegacyNavigationDirectionUsedInStrictMode.logForStrictMode(fromContext.controller, args)
                host.setBackstack(
                    host.backstackFlow.value
                        .let {
                            if(isReplace) it.close() else it
                        }
                        .plus(
                            instruction
                        )
                )
            }
            else -> throw IllegalStateException()
        }
    }

    override fun close(context: NavigationContext<out Fragment>) {
        val parentContext = context.parentContext()
        val container = parentContext
            ?.containerManager
            ?.containers
            ?.firstOrNull {
                it.backstack.any { it.instructionId == context.getNavigationHandle().id }
            }

        if(container == null) {
            /*
             * There are some cases where a Fragment's FragmentManager can be removed from the Fragment.
             * There is (as far as I am aware) no easy way to check for the FragmentManager being removed from the
             * Fragment, other than attempting to catch the exception that is thrown in the case of a missing
             * parentFragmentManager.
             *
             * If a Fragment's parentFragmentManager has been destroyed or removed, there's very little we can
             * do to resolve the problem, and the most likely case is if
             *
             * The most common case where this can occur is if a DialogFragment is closed in response
             * to a nested Fragment closing with a result - this causes the DialogFragment to close,
             * and then for the nested Fragment to attempt to close immediately afterwards, which fails because
             * the nested Fragment is no longer attached to any fragment manager (and won't be again).
             *
             * see ResultTests.whenResultFlowIsLaunchedInDialogFragment_andCompletesThroughTwoNestedFragments_thenResultIsDelivered
             */
            runCatching {
                context.fragment.parentFragmentManager
            }
            .onSuccess { fragmentManager ->
                runCatching {  fragmentManager.executePendingTransactions() }
                    .onFailure {
                        // if we failed to execute pending transactions, we're going to
                        // re-attempt to close this context (by executing "close" on it's NavigationHandle)
                        // but we're going to delay for 1 millisecond first, which will allow the
                        // main thread to finish executing the transaction before attempting the close
                        val navigationHandle = context.fragment.getNavigationHandle()
                        navigationHandle.lifecycleScope.launch {
                            delay(1)
                            navigationHandle.close()
                        }
                    }
                    .onSuccess {
                        fragmentManager.commitNow {
                            setReorderingAllowed(true)
                            remove(context.contextReference)
                        }
                    }
            }
            return
        }

        container.setBackstack(
            container.backstackFlow.value.close(
                context.getNavigationHandle().id
            )
        )
    }

    @Deprecated("Please create a fragment and use `fragment.arguments = Bundle().addOpenInstruction(instruction)` yourself")
    public fun createFragment(
        fragmentManager: FragmentManager,
        binding: NavigationBinding<*, *>,
        instruction: AnyOpenInstruction
    ): Fragment {
        val fragment = fragmentManager.fragmentFactory.instantiate(
            binding.destinationType.java.classLoader!!,
            binding.destinationType.java.name
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
    val open = fromContext.controller.dependencyScope.get<ExecuteOpenInstruction>()
    val hostInstructionAs = fromContext.controller.dependencyScope.get<HostInstructionAs>()

    open.invoke(
        fromContext,
        hostInstructionAs<Activity>(
            fromContext,
            instruction.asDirection(navigationDirection)
        ),
    )
}