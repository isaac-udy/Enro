package dev.enro.core.result.flows

import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationKey

public class NavigationFlowScope internal constructor(
    @PublishedApi
    internal val resultManager: FlowResultManager
) {
    @PublishedApi
    internal val steps: MutableList<FlowStep> = mutableListOf()

    public inline fun <reified T : Any> push(
        dependsOn: List<Any> = emptyList(),
        noinline key: () -> NavigationKey.SupportsPush.WithResult<T>,
    ): T {
        val baseId = key::class.java.name
        val count = steps.count { it.stepId.startsWith(baseId) }
        val step = FlowStep(
            stepId = "$baseId@$count",
            key = key(),
            dependsOn = dependsOn.contentHash(),
            direction = NavigationDirection.Push,
        )
        steps.add(step)

        val completedStep = resultManager.results[step.stepId]?.let {
            if (it.result !is T) {
                resultManager.results.remove(it.stepId)
                return@let null
            }
            if (it.dependsOn != step.dependsOn) {
                resultManager.results.remove(it.stepId)
                return@let null
            }
            it
        }
        return completedStep?.result as? T ?: throw NoResultForPush(step)
    }

    public inline fun <reified T : Any> present(
        dependsOn: List<Any> = emptyList(),
        noinline key: () -> NavigationKey.SupportsPresent.WithResult<T>,
    ): T {
        val baseId = key::class.java.name
        val count = steps.count { it.stepId.startsWith(baseId) }
        val step = FlowStep(
            stepId = "$baseId@$count",
            key = key(),
            dependsOn = dependsOn.contentHash(),
            direction = NavigationDirection.Present,
        )
        steps.add(step)

        val completedStep = resultManager.results[step.stepId]?.let {
            if (it.result !is T) {
                resultManager.results.remove(it.stepId)
                return@let null
            }
            if (it.dependsOn != step.dependsOn) {
                resultManager.results.remove(it.stepId)
                return@let null
            }
            it
        }
        return completedStep?.result as? T ?: throw NoResultForPresent(step)
    }

    public fun escape(): Nothing {
        throw Escape()
    }

    @PublishedApi
    internal class NoResultForPush(val step: FlowStep) : RuntimeException()

    @PublishedApi
    internal class NoResultForPresent(val step: FlowStep) : RuntimeException()

    @PublishedApi
    internal class Escape : RuntimeException()
}
