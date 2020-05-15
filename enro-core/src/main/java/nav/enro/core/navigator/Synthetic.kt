package nav.enro.core.navigator

import nav.enro.core.NavigationInstruction
import nav.enro.core.NavigationKey
import nav.enro.core.context.NavigationContext

interface SyntheticDestination<T : NavigationKey> {
    fun process(
        navigationContext: NavigationContext<out Any, out NavigationKey>,
        instruction: NavigationInstruction.Open<T>
    )
}
