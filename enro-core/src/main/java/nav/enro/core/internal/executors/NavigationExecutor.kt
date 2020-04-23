package nav.enro.core.internal.executors

import nav.enro.core.NavigationInstruction
import nav.enro.core.Navigator
import nav.enro.core.internal.context.NavigationContext

internal abstract class NavigationExecutor {
    internal abstract fun open(
        navigator: Navigator<*>,
        fromContext: NavigationContext<out Any, *>,
        instruction: NavigationInstruction.Open<*>
    )

    internal abstract fun close(
        context: NavigationContext<out Any, *>
    )
}

