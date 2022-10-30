package dev.enro.core.compose

import androidx.compose.material.ExperimentalMaterialApi
import androidx.fragment.app.FragmentActivity
import dev.enro.core.*
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.container.add
import dev.enro.core.container.asPresentInstruction
import dev.enro.core.container.asPushInstruction
import dev.enro.core.container.close
import dev.enro.core.controller.usecase.ExecuteOpenInstruction
import dev.enro.core.hosts.OpenComposableInFragment
import dev.enro.core.hosts.OpenInstructionInActivity
import dev.enro.core.internal.get

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
                host.setBackstack(
                    host.backstackFlow.value
                        .let {
                            if (isReplace) it.close() else it
                        }
                        .add(instruction)
                )
            }
            else -> throw IllegalStateException()
        }
    }

    override fun close(context: NavigationContext<out ComposableDestination>) {
        val container = context.contextReference.owner.parentContainer
        container.setBackstack(container.backstackFlow.value.close(context.contextReference.owner.instruction.instructionId))
    }
}

private fun <T : NavigationDirection> NavigationInstruction.Open<T>.asFragmentHostInstruction(isRoot: Boolean) =
    NavigationInstruction.Open.OpenInternal(
        navigationDirection,
        OpenComposableInFragment(this, isRoot = isRoot)
    )

private fun openComposableAsActivity(
    fromContext: NavigationContext<out Any>,
    direction: NavigationDirection,
    instruction: AnyOpenInstruction
) {
    val fragmentInstruction = instruction.asFragmentHostInstruction(isRoot = true)
    fromContext.controller.dependencyScope.get<ExecuteOpenInstruction>().invoke(
        fromContext,
        NavigationInstruction.Open.OpenInternal(
            direction,
            OpenInstructionInActivity(fragmentInstruction)
        )
    )
}

private fun openComposableAsFragment(
    fromContext: NavigationContext<out Any>,
    instruction: AnyOpenInstruction
) {
    val fragmentInstruction = instruction.asFragmentHostInstruction(isRoot = false)
    fromContext.controller.dependencyScope.get<ExecuteOpenInstruction>().invoke(
        fromContext,
        fragmentInstruction
    )
}