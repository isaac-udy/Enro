package dev.enro.core.synthetic

import dev.enro.core.*

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