package nav.enro.core.internal.executors

import nav.enro.core.NavigationInstruction
import nav.enro.core.Navigator
import nav.enro.core.internal.context.NavigationContext

internal interface NavigationExecutor {

    fun open(
        navigator: Navigator<*>,
        fromContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open
    )

    fun close(
        context: NavigationContext<*>
    )

}

