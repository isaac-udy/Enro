package dev.enro.synthetic

import dev.enro.core.*

public object DefaultSyntheticExecutor :
    NavigationExecutor<Any, SyntheticDestination<*>, NavigationKey>(
        fromType = Any::class,
        opensType = SyntheticDestination::class,
        keyType = NavigationKey::class
    ) {
    override fun open(args: ExecutorArgs<out Any, out SyntheticDestination<*>, out NavigationKey>) {
        args.binding as SyntheticNavigationBinding<NavigationKey>

        val destination = args.binding.destination.invoke()
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