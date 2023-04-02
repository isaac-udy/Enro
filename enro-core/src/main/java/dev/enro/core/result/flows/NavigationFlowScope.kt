package dev.enro.core.result.flows

import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationKey

public class NavigationFlowScope internal constructor(
    @PublishedApi
    internal val resultManager: FlowResultManager
) {
    @PublishedApi
    internal val steps: MutableList<FlowStep<out Any>> = mutableListOf()

    public inline fun <reified T : Any> push(
        noinline block: FlowStepBuilderScope<T>.() -> NavigationKey.SupportsPush.WithResult<T>,
    ): T = step(
        direction = NavigationDirection.Push,
        block = block,
    )

    public inline fun <reified T : Any> present(
        noinline block: FlowStepBuilderScope<T>.() -> NavigationKey.SupportsPresent.WithResult<T>,
    ): T = step(
        direction = NavigationDirection.Present,
        block = block,
    )

    @PublishedApi
    internal inline fun <reified T: Any> step(
        direction: NavigationDirection,
        noinline block: FlowStepBuilderScope<T>.() -> NavigationKey.WithResult<T>,
    ) : T {
        val baseId = block::class.java.name
        val count = steps.count { it.stepId.startsWith(baseId) }
        val builder = FlowStepBuilder<T>()
        val key = builder.scope.run(block)
        val step = builder.build(
            stepId = "$baseId@$count",
            navigationKey = key,
            navigationDirection = direction,
        )
        val defaultResult = builder.getDefaultResult()
        if (defaultResult != null) {
            resultManager.setDefault(step, defaultResult)
        }
        steps.add(step)
        val result = resultManager.get(step)
        return result ?: throw NoResult(step)
    }

    public fun escape(): Nothing {
        throw Escape()
    }

    @PublishedApi
    internal class NoResult(val step: FlowStep<out Any>) : RuntimeException()

    @PublishedApi
    internal class Escape : RuntimeException()
}
