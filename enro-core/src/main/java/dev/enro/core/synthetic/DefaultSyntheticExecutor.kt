package dev.enro.core.synthetic

import dev.enro.core.*

object DefaultSyntheticExecutor : NavigationExecutor<Any, SyntheticDestination<*>, NavigationKey>(
    fromType = Any::class,
    opensType = SyntheticDestination::class,
    keyType = NavigationKey::class
) {
    override fun open(args: ExecutorArgs<out Any, out SyntheticDestination<*>, out NavigationKey>) {
        args.navigator as SyntheticNavigator<NavigationKey>

        val destination = args.navigator.destination.invoke()
        destination.bind(
            args.fromContext,
            args.instruction
        )
        destination.process()
    }

    override fun close(context: NavigationContext<out SyntheticDestination<*>>) {
        throw EnroException.UnreachableState()
    }
}