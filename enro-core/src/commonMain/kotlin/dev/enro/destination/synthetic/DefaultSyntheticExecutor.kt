package dev.enro.destination.synthetic

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey

public object DefaultSyntheticExecutor  {
    internal fun open(
        fromContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>,
        binding: SyntheticNavigationBinding<out NavigationKey>
    ) {
        val destination = binding.destination.invoke()
        destination.bind(
            fromContext,
            instruction,
        )
        destination.process()
    }
}