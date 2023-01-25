package dev.enro.core.compose

import android.app.Activity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dev.enro.core.*
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.container.*
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.ExecuteOpenInstruction
import dev.enro.core.controller.usecase.HostInstructionAs

public object DefaultComposableExecutor :
    NavigationExecutor<Any, ComposableDestination, NavigationKey>(
        fromType = Any::class,
        opensType = ComposableDestination::class,
        keyType = NavigationKey::class
    ) {
    @OptIn(ExperimentalMaterialApi::class)
    override fun open(args: ExecutorArgs<out Any, out ComposableDestination, out NavigationKey>) {
        val fromContext = args.fromContext

        val isReplace = args.instruction.navigationDirection is NavigationDirection.Replace
        val isDialog =
            DialogDestination::class.java.isAssignableFrom(args.binding.destinationType.java)
                    || BottomSheetDestination::class.java.isAssignableFrom(args.binding.destinationType.java)

        val instruction = when (args.instruction.navigationDirection) {
            is NavigationDirection.Replace,
            is NavigationDirection.Forward -> when {
                isDialog -> args.instruction.asPresentInstruction()
                else -> args.instruction.asPushInstruction()
            }
            else -> args.instruction
        }

        when (instruction.navigationDirection) {
            NavigationDirection.ReplaceRoot -> {
                openComposableAsActivity(args.fromContext, NavigationDirection.ReplaceRoot, instruction)
            }
            NavigationDirection.Present,
            NavigationDirection.Push  -> {

                val containerManager = args.fromContext.containerManager

                val host = containerManager.activeContainer?.takeIf {
                    it.isVisible && it.accept(instruction)
                } ?: containerManager.containers
                        .filter { it.isVisible }
                        .firstOrNull { it.accept(instruction) }

                if (host == null) {
                    val parentContext = args.fromContext.parentContext()
                    if (parentContext == null) {
                        if (fromContext.activity is FragmentActivity) {
                            openComposableAsFragment(
                                args.fromContext,
                                instruction
                            )
                        } else {
                            openComposableAsActivity(
                                args.fromContext,
                                NavigationDirection.Present,
                                instruction
                            )
                        }
                        if (isReplace) {
                            fromContext.getNavigationHandle().close()
                        }
                    } else {
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

                EnroException.LegacyNavigationDirectionUsedInStrictMode.logForStrictMode(
                    fromContext.controller,
                    args
                )
                host.setBackstack { backstack ->
                    backstack
                        .let {
                            if (isReplace) it.dropLast(1) else it
                        }
                        .plus(instruction)
                }
            }
            else -> throw IllegalStateException()
        }
    }

    override fun close(context: NavigationContext<out ComposableDestination>) {
        val container = context.contextReference.owner.parentContainer
        container.setBackstack(container.backstackFlow.value.close(context.contextReference.owner.instruction.instructionId))
    }
}

private fun openComposableAsActivity(
    fromContext: NavigationContext<out Any>,
    direction: NavigationDirection,
    instruction: AnyOpenInstruction
) {
    val open = fromContext.controller.dependencyScope.get<ExecuteOpenInstruction>()
    val hostInstructionAs = fromContext.controller.dependencyScope.get<HostInstructionAs>()

    open(
        navigationContext = fromContext,
        instruction = hostInstructionAs<Activity>(fromContext, instruction.asDirection(direction))
    )
}

private fun openComposableAsFragment(
    fromContext: NavigationContext<out Any>,
    instruction: AnyOpenInstruction
) {
    val open = fromContext.controller.dependencyScope.get<ExecuteOpenInstruction>()
    val hostInstructionAs = fromContext.controller.dependencyScope.get<HostInstructionAs>()

    open(
        navigationContext = fromContext,
        instruction = hostInstructionAs<Fragment>(fromContext, instruction)
    )
}