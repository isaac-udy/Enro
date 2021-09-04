package dev.enro.core.compose

import dev.enro.core.*
import dev.enro.core.fragment.internal.fragmentHostFor

object DefaultComposableExecutor : NavigationExecutor<Any, ComposableDestination, NavigationKey>(
    fromType = Any::class,
    opensType = ComposableDestination::class,
    keyType = NavigationKey::class
) {
    override fun open(args: ExecutorArgs<out Any, out ComposableDestination, out NavigationKey>) {
        val host = args.fromContext.composeHostFor(args.key)

        // TODO seperate destinations here? Should these go to different fragments?
        val isDialog = DialogDestination::class.java.isAssignableFrom(args.navigator.contextType.java)
                || BottomSheetDestination::class.java.isAssignableFrom(args.navigator.contextType.java)

        if(isDialog) {
            args.fromContext.controller.open(
                args.fromContext,
                NavigationInstruction.Open.OpenInternal(
                    args.instruction.navigationDirection,
                    ComposeDialogFragmentHostKey(args.instruction)
                )
            )
            return
        }

        if(host == null || args.instruction.navigationDirection == NavigationDirection.REPLACE_ROOT) {
            val fragmentHost = if(args.instruction.navigationDirection == NavigationDirection.REPLACE_ROOT) null else args.fromContext.fragmentHostFor(args.key)
            args.fromContext.controller.open(
                args.fromContext,
                NavigationInstruction.Open.OpenInternal(
                    args.instruction.navigationDirection,
                    ComposeFragmentHostKey(args.instruction, fragmentHost?.containerId)
                )
            )
            return
        }

        host.containerController.push(args.instruction)
    }

    override fun close(context: NavigationContext<out ComposableDestination>) {
        context.contextReference.contextReference.requireParentContainer().close()
    }
}