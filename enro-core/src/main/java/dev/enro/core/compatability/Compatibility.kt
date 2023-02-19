package dev.enro.core.compatability

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import dev.enro.core.*
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.container.*
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.ExecuteOpenInstruction
import dev.enro.core.controller.usecase.HostInstructionAs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal object Compatibility {

    object DefaultContainerExecutor {
        private const val ORIGINAL_NAVIGATION_DIRECTION = "Compatibility.DefaultContainerExecutor.ORIGINAL_NAVIGATION_DIRECTION"

        fun earlyExitForFragments(args: ExecutorArgs<*, *, *>): Boolean {
            return args.fromContext is FragmentContext && !args.fromContext.fragment.isAdded
        }

        fun earlyExitForReplace(args: ExecutorArgs<*, *, *>): Boolean {
            val isReplace = args.instruction.navigationDirection is NavigationDirection.Replace

            val isReplaceActivity = args.fromContext is ActivityContext && isReplace
            if (!isReplaceActivity) return false

            openInstructionAsActivity(args.fromContext, NavigationDirection.Present, args.instruction)
            args.fromContext.activity.finish()
            return true
        }

        internal fun getInstructionForCompatibility(args: ExecutorArgs<*, *, *>): NavigationInstruction.Open<*> {
            EnroException.LegacyNavigationDirectionUsedInStrictMode.logForStrictMode(args.fromContext.controller, args)
            val isDialog = isDialog(args)
            return when (args.instruction.navigationDirection) {
                is NavigationDirection.Replace,
                is NavigationDirection.Forward -> {
                    when {
                        isDialog -> args.instruction.asPresentInstruction()
                        else -> args.instruction.asPushInstruction()
                    }.apply {
                        additionalData.putParcelable(ORIGINAL_NAVIGATION_DIRECTION, args.instruction.navigationDirection)
                    }
                }
                else -> args.instruction
            }
        }

        internal fun earlyExitForMissingContainerPush(
            fromContext: NavigationContext<*>,
            instruction: AnyOpenInstruction,
            container: NavigationContainer?,
            findContainerFor: (NavigationContext<*>, AnyOpenInstruction) -> NavigationContainer?,
        ): Boolean {
            if (instruction.navigationDirection != NavigationDirection.Push) return false
            if (container != null) return false

            EnroException.MissingContainerForPushInstruction.logForStrictMode(
                fromContext.controller,
                instruction.navigationKey,
            )
            val presentInstruction = instruction.asPresentInstruction()
            val presentContainer =
                findContainerFor(
                    fromContext.rootContext(),
                    presentInstruction
                )

            val originalDirection = instruction.additionalData
                .getParcelableCompat<NavigationDirection>(ORIGINAL_NAVIGATION_DIRECTION)
            val isReplace = originalDirection == NavigationDirection.Replace

            requireNotNull(presentContainer)
            presentContainer.setBackstack { backstack ->
                backstack
                    .let { if (isReplace) it.pop() else it }
                    .plus(presentInstruction)
            }
            return true
        }

        internal fun earlyExitForNoContainer(context: NavigationContext<*>) : Boolean {
            if (context.contextReference !is Fragment) return false

            val container = context.parentContainer()
            if (container != null) return false

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
                context.contextReference.parentFragmentManager
            }
                .onSuccess { fragmentManager ->
                    runCatching { fragmentManager.executePendingTransactions() }
                        .onFailure {
                            // if we failed to execute pending transactions, we're going to
                            // re-attempt to close this context (by executing "close" on it's NavigationHandle)
                            // but we're going to delay for 1 millisecond first, which will allow the
                            // main thread to finish executing the transaction before attempting the close
                            val navigationHandle = context.contextReference.getNavigationHandle()
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
            return true
        }
    }

    object NavigationContainer {
        fun processBackstackForDeprecatedInstructionTypes(
            backstack: NavigationBackstack,
            acceptsNavigationKey: (NavigationKey) -> Boolean,
        ): NavigationBackstack {
            return backstack.mapIndexed { i, it ->
                when {
                    it.navigationDirection !is NavigationDirection.Forward -> it
                    i == 0 || acceptsNavigationKey(it.navigationKey) -> it.asPushInstruction()
                    else -> it.asPresentInstruction()
                }
            }.toBackstack()
        }
    }
}

private fun openInstructionAsActivity(
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

@OptIn(ExperimentalMaterialApi::class)
private fun isDialog(args: ExecutorArgs<*, *, *>): Boolean {
    return DialogFragment::class.java.isAssignableFrom(args.binding.destinationType.java) ||
            DialogDestination::class.java.isAssignableFrom(args.binding.destinationType.java)
            || BottomSheetDestination::class.java.isAssignableFrom(args.binding.destinationType.java)

}

private inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String): T? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}