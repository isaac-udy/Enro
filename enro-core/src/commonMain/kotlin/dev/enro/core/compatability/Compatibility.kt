package dev.enro.compatability

import android.app.Activity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import androidx.savedstate.read
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import androidx.savedstate.write
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.EnroException
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.activity
import dev.enro.core.activity.ActivityNavigationContainer
import dev.enro.core.close
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationInstructionFilter
import dev.enro.core.container.asDirection
import dev.enro.core.container.asPresentInstruction
import dev.enro.core.container.asPushInstruction
import dev.enro.core.container.pop
import dev.enro.core.container.setBackstack
import dev.enro.core.container.toBackstack
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.ExecuteOpenInstruction
import dev.enro.core.controller.usecase.HostInstructionAs
import dev.enro.core.getNavigationHandle
import dev.enro.core.navigationContext
import dev.enro.core.parentContainer
import dev.enro.core.rootContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.full.isSubclassOf
import dev.enro.core.container.NavigationContainer as RealNavigationContainer

internal object Compatibility {

    object DefaultContainerExecutor {
        private const val COMPATIBILITY_NAVIGATION_DIRECTION = "Compatibility.DefaultContainerExecutor.COMPATIBILITY_NAVIGATION_DIRECTION"

        fun earlyExitForFragments(fromContext: NavigationContext<*>): Boolean {
            return fromContext.contextReference is Fragment && !fromContext.contextReference.isAdded
        }

        fun earlyExitForReplace(
            fromContext: NavigationContext<*>,
            instruction: AnyOpenInstruction,
        ): Boolean {
            val isReplace = instruction.navigationDirection is NavigationDirection.Replace

            val isReplaceActivity = fromContext.contextReference is Activity && isReplace
            if (!isReplaceActivity) return false

            openInstructionAsActivity(fromContext, NavigationDirection.Present, instruction)
            fromContext.activity.finish()
            return true
        }

        internal fun getInstructionForCompatibility(
            binding: NavigationBinding<*, *>,
            fromContext: NavigationContext<*>,
            instruction: AnyOpenInstruction,
        ): NavigationInstruction.Open<*> {
            EnroException.LegacyNavigationDirectionUsedInStrictMode.logForStrictMode(fromContext.controller, instruction)
            val isDialog = isDialog(binding)
            return when (instruction.navigationDirection) {
                is NavigationDirection.Replace,
                is NavigationDirection.Forward -> {
                    when {
                        isDialog -> instruction.asPresentInstruction()
                        else -> instruction.asPushInstruction()
                    }.apply {
                        extras.write {
                            putSavedState(COMPATIBILITY_NAVIGATION_DIRECTION, encodeToSavedState(instruction.navigationDirection))
                        }
                    }
                }
                else -> instruction
            }
        }

        internal fun earlyExitForMissingContainerPush(
            fromContext: NavigationContext<*>,
            instruction: AnyOpenInstruction,
            container: RealNavigationContainer?,
        ): Boolean {
            if (instruction.navigationDirection != NavigationDirection.Push) return false
            if (container != null) return false

            EnroException.MissingContainerForPushInstruction.logForStrictMode(
                fromContext.controller,
                instruction.navigationKey,
            )
            val presentInstruction = instruction.asPresentInstruction()
            val presentContainer = getPresentationContainerForLegacyInstruction(
                fromContext,
                presentInstruction
            )

            val originalDirection = instruction.extras.read {
                decodeFromSavedState<NavigationDirection>(
                    getSavedStateOrNull(COMPATIBILITY_NAVIGATION_DIRECTION) ?: return@read null
                )
            }
            val isReplace = originalDirection == NavigationDirection.Replace

            presentContainer.setBackstack { backstack ->
                backstack
                    .let { if (isReplace) it.pop() else it }
                    .plus(presentInstruction)
            }
            return true
        }

        private fun getPresentationContainerForLegacyInstruction(
            fromContext: NavigationContext<*>,
            instruction: AnyOpenInstruction,
        ): RealNavigationContainer {
            val context = fromContext.rootContext()
            val defaultFragmentContainer = context
                .containerManager
                .containers
                .firstOrNull { it.key == NavigationContainerKey.FromId(android.R.id.content) }

            val useDefaultFragmentContainer = defaultFragmentContainer != null &&
                    defaultFragmentContainer.accept(instruction)

            return when {
                useDefaultFragmentContainer -> requireNotNull(defaultFragmentContainer)
                else -> ActivityNavigationContainer(fromContext.activity.navigationContext)
            }
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
            navigationInstructionFilter: NavigationInstructionFilter,
        ): NavigationBackstack {
            return backstack.mapIndexed { i, it ->
                when {
                    it.navigationDirection !is NavigationDirection.Forward -> it
                    i == 0 || navigationInstructionFilter.accept(it.asPushInstruction()) -> it.asPushInstruction()
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

private fun isDialog(
    binding: NavigationBinding<*, *>,
): Boolean {
    return binding.destinationType.isSubclassOf(DialogFragment::class)
}