package dev.enro.core.compose

import androidx.compose.material.ExperimentalMaterialApi
import dev.enro.core.*
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.container.asPresentInstruction
import dev.enro.core.container.asPushInstruction
import dev.enro.core.container.close
import dev.enro.core.container.add
import dev.enro.core.hosts.OpenComposableInFragment
import dev.enro.core.hosts.OpenInstructionInActivity

object DefaultComposableExecutor : NavigationExecutor<Any, ComposableDestination, NavigationKey>(
    fromType = Any::class,
    opensType = ComposableDestination::class,
    keyType = NavigationKey::class
) {
    @OptIn(ExperimentalMaterialApi::class)
    override fun open(args: ExecutorArgs<out Any, out ComposableDestination, out NavigationKey>) {
        val fromContext = args.fromContext

        val isReplace = args.instruction.navigationDirection is NavigationDirection.Replace
        val isDialog = DialogDestination::class.java.isAssignableFrom(args.navigator.contextType.java)
                || BottomSheetDestination::class.java.isAssignableFrom(args.navigator.contextType.java)

        val instruction = when(args.instruction.navigationDirection) {
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
                        openComposableAsActivity(args.fromContext, NavigationDirection.Present, instruction)
                        if(isReplace) {
                            fromContext.getNavigationHandle().close()
                        }
                    } else {
                        parentContext.controller.open(
                            parentContext,
                            args.instruction
                        )
                    }
                    return
                }

                EnroException.LegacyNavigationDirectionUsedInStrictMode.logForStrictMode(fromContext.controller, args)
                 host.setBackstack(
                    host.backstackFlow.value
                        .let {
                            if(isReplace) it.close() else it
                        }
                        .add(instruction)
                )

            }
            else -> throw IllegalStateException()
        }
    }

    override fun close(context: NavigationContext<out ComposableDestination>) {
        val container = context.contextReference.contextReference.parentContainer
        container.setBackstack(container.backstackFlow.value.close(context.contextReference.contextReference.instruction.instructionId))
    }
}

private fun <T: NavigationDirection> NavigationInstruction.Open<T>.asFragmentHostInstruction() = NavigationInstruction.Open.OpenInternal(
    navigationDirection,
    OpenComposableInFragment(this, isRoot = true)
)

private fun openComposableAsActivity(
    fromContext: NavigationContext<out Any>,
    direction: NavigationDirection,
    instruction: AnyOpenInstruction
) {
    val fragmentInstruction = instruction.asFragmentHostInstruction()
    fromContext.controller.open(
        fromContext,
        NavigationInstruction.Open.OpenInternal(
            direction,
            OpenInstructionInActivity(fragmentInstruction)
        )
    )
}
