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

        host.containerState.push(args.instruction)
    }

    override fun close(context: NavigationContext<out ComposableDestination>) {
        context.contextReference.containerState.close()
    }
}