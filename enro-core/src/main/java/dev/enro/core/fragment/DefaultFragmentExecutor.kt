package dev.enro.core.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.*
import androidx.lifecycle.lifecycleScope
import dev.enro.core.*
import dev.enro.core.container.asPresentInstruction
import dev.enro.core.container.asPushInstruction
import dev.enro.core.fragment.container.FragmentNavigationContainer
import dev.enro.core.fragment.internal.SingleFragmentKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
            EnroException.LegacyNavigationDirectionUsedInStrictMode.logForStrictMode(fromContext.controller, args)
            openFragmentAsActivity(fromContext, instruction.navigationDirection, instruction)
            return
        }

        when (instruction.navigationDirection) {
            NavigationDirection.ReplaceRoot -> {
                openFragmentAsActivity(fromContext, instruction.navigationDirection, instruction)
            }
            NavigationDirection.Present -> {
                when {
                    isDialog -> openFragmentAsDialog(fromContext, navigator, instruction)
                    else -> openFragmentAsActivity(fromContext, instruction.navigationDirection, instruction)
                }
                EnroException.LegacyNavigationDirectionUsedInStrictMode.logForStrictMode(fromContext.controller, args)
                if(isReplace) {
                    fromContext.getNavigationHandle().close()
                }
            }
            NavigationDirection.Push -> {
                val containerManager = args.fromContext.containerManager
                val host = containerManager.activeContainer?.takeIf {
                    it.isVisible && it.accept(args.key)
                } ?: args.fromContext.containerManager.containers
                        .filter { it.isVisible }
                        .filterIsInstance<FragmentNavigationContainer>()
                        .firstOrNull { it.accept(args.key) }

                if (host == null) {
                    val parentContext = fromContext.parentContext()
                    if(parentContext == null) {
                        EnroException.MissingContainerForPushInstruction.logForStrictMode(
                            fromContext.controller,
                            args
                        )
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

                EnroException.LegacyNavigationDirectionUsedInStrictMode.logForStrictMode(fromContext.controller, args)
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
