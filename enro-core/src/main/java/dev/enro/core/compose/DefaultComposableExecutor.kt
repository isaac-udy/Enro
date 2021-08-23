package dev.enro.core.compose

import dev.enro.core.*

object DefaultComposableExecutor : NavigationExecutor<Any, ComposableDestination, NavigationKey>(
    fromType = Any::class,
    opensType = ComposableDestination::class,
    keyType = NavigationKey::class
) {
    override fun open(args: ExecutorArgs<out Any, out ComposableDestination, out NavigationKey>) {
        val host = args.fromContext.composeHostFor(args.key)

        if(host == null || args.instruction.navigationDirection == NavigationDirection.REPLACE_ROOT) {
            args.fromContext.controller.open(
                args.fromContext,
                NavigationInstruction.Open.OpenInternal(
                    args.instruction.navigationDirection,
                    HostedComposeKey(args.instruction)
                )
            )
            return
        }

        host.containerState.push(args.instruction)
        return
    }

    override fun close(context: NavigationContext<out ComposableDestination>) {
        context.contextReference.containerState.close()
    }
}