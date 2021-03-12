package dev.enro.core.synthetic

import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.NavigationContext

interface SyntheticDestination<T : NavigationKey> {
    fun process(
        navigationContext: NavigationContext<out Any>,
        key: T,
        instruction: NavigationInstruction.Open
    )
}
