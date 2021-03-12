package dev.enro.core.synthetic

import androidx.fragment.app.Fragment
import nav.enro.core.ExecutorArgs
import nav.enro.core.NavigationContext
import nav.enro.core.NavigationExecutor
import nav.enro.core.NavigationKey
import java.lang.IllegalStateException

object DefaultSyntheticExecutor : NavigationExecutor<Any, SyntheticDestination<*>, NavigationKey>(
    fromType = Any::class,
    opensType = SyntheticDestination::class,
    keyType = NavigationKey::class
) {
    override fun open(args: ExecutorArgs<out Any, out SyntheticDestination<*>, out NavigationKey>) {
        args.navigator as SyntheticNavigator<NavigationKey>

        args.navigator.destination.process(
            args.fromContext,
            args.key,
            args.instruction
        )
    }

    override fun close(context: NavigationContext<out SyntheticDestination<*>>) {
        throw IllegalStateException("Synthetic Destinations should not ever execute a 'close' instruction")
    }
}