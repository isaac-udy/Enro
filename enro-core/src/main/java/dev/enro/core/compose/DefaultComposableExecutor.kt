package dev.enro.core.compose

import androidx.compose.material.ExperimentalMaterialApi
import dev.enro.core.*
import dev.enro.core.compose.container.ComposableNavigationContainer
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.dialog.ComposeDialogFragmentHostKey
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.fragment.container.FragmentNavigationContainer
import dev.enro.core.fragment.internal.SingleFragmentKey

object DefaultComposableExecutor : NavigationExecutor<Any, ComposableDestination, NavigationKey>(
    fromType = Any::class,
    opensType = ComposableDestination::class,
    keyType = NavigationKey::class
) {
    @OptIn(ExperimentalMaterialApi::class)
    override fun open(args: ExecutorArgs<out Any, out ComposableDestination, out NavigationKey>) {
        val isDialog = DialogDestination::class.java.isAssignableFrom(args.navigator.contextType.java)
                || BottomSheetDestination::class.java.isAssignableFrom(args.navigator.contextType.java)

        when (args.instruction.navigationDirection) {
            is NavigationDirection.Present -> {
                if(isDialog) {
                    args.instruction as OpenPresentInstruction
                    args.fromContext.controller.open(
                        args.fromContext,
                        NavigationInstruction.Open.OpenInternal(
                            args.instruction.navigationDirection,
                            ComposeDialogFragmentHostKey(args.instruction)
                        )
                    )
                }
                else {
                    openComposableAsActivity(args.fromContext, NavigationDirection.Present, args.instruction)
                }
            }
            NavigationDirection.ReplaceRoot -> {
                openComposableAsActivity(args.fromContext, NavigationDirection.ReplaceRoot, args.instruction)
            }
            NavigationDirection.Push  -> {
                args.instruction as OpenForwardInstruction
                val containerManager = args.fromContext.containerManager
                val host = containerManager.activeContainer?.takeIf { it.accept(args.key) }
                    ?: args.fromContext.containerManager.containers
                        .firstOrNull { it.accept(args.key) }

                if (host == null) {
                    val parentContext = args.fromContext.parentContext()
                    if (parentContext == null) {
                        openComposableAsActivity(args.fromContext, NavigationDirection.Present, args.instruction)
                    } else {
                        parentContext.controller.open(
                            parentContext,
                            args.instruction
                        )
                    }
                    return
                }

                when (host) {
                    is ComposableNavigationContainer -> host.setBackstack(
                        host.backstackFlow.value.push(args.instruction)
                    )
                    is FragmentNavigationContainer -> host.setBackstack(
                        host.backstackFlow.value.push(args.instruction.asFragmentHostInstruction())
                    )
                }
            }
        }
    }

    override fun close(context: NavigationContext<out ComposableDestination>) {
        val container = context.contextReference.contextReference.requireParentContainer()
        container.setBackstack(container.backstackFlow.value.close())
    }
}

private fun <T: NavigationDirection> NavigationInstruction.Open<T>.asFragmentHostInstruction() = NavigationInstruction.Open.OpenInternal(
    navigationDirection,
    ComposeFragmentHostKey(this)
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
            SingleFragmentKey(fragmentInstruction)
        )
    )
}
