package dev.enro.core.compose

import dev.enro.core.*

object DefaultComposableExecutor : NavigationExecutor<Any, ComposableDestination, NavigationKey>(
    fromType = Any::class,
    opensType = ComposableDestination::class,
    keyType = NavigationKey::class
) {
    override fun open(args: ExecutorArgs<out Any, out ComposableDestination, out NavigationKey>) {
        if(args.fromContext !is ComposeContext) {
            args.fromContext.controller.open(
                args.fromContext,
                NavigationInstruction.Open.OpenInternal(
                    args.instruction.navigationDirection,
                    HostedComposeKey(args.instruction)
                )
            )
            return
        }

        if(args.instruction.navigationDirection == NavigationDirection.REPLACE_ROOT) {
            args.fromContext.controller.open(
                args.fromContext,
                NavigationInstruction.Open.OpenInternal(
                    args.instruction.navigationDirection,
                    HostedComposeKey(args.instruction)
                )
            )
            return
        }

        args.fromContext.contextReference as ComposableDestination
        args.fromContext.contextReference.container.push(args.instruction)
    }

    override fun close(context: NavigationContext<out ComposableDestination>) {
        context.contextReference.container.close()
    }
}