package dev.enro.result.flow

import dev.enro.NavigationKey
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.platform.callSiteClassName
import dev.enro.withMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.jvm.JvmName

public open class NavigationFlowScope internal constructor(
    @PublishedApi
    internal val flow: NavigationFlow<*>,
    @PublishedApi
    internal val coroutineScope: CoroutineScope,
    @PublishedApi
    internal val resultManager: FlowResultManager,
    public val navigationFlowReference: NavigationFlowReference,
    @PublishedApi
    internal val steps: MutableList<FlowStep<Any>> = mutableListOf(),
    @PublishedApi
    internal val suspendingSteps: MutableList<String> = mutableListOf(),
) {

    public inline fun <reified K : NavigationKey> open(
        key: K,
        noinline block: FlowStepDefinition.ConfigurationScope<K>.() -> Unit = {},
    ) {
        return open(key.withMetadata(), block)
    }

    public inline fun <reified K : NavigationKey> open(
        key: NavigationKey.WithMetadata<K>,
        noinline block: FlowStepDefinition.ConfigurationScope<K>.() -> Unit = {},
    ) {
        return step(
            stepDefinition = object : FlowStepDefinition<K, Unit>() {
                override val keyWithMetadata = key
                override val result = Unit::class

                init { ConfigurationScope<K>(this).block() }
            },
        )
    }

    public inline fun <reified K: NavigationKey.WithResult<R>, reified R : Any> open(
        key: K,
        noinline block: FlowStepDefinition.ConfigurationScope<K>.() -> Unit = {},
    ): R {
        return open(key.withMetadata(), block)
    }

    public inline fun <reified K: NavigationKey.WithResult<R>, reified R : Any> open(
        key: NavigationKey.WithMetadata<K>,
        noinline block: FlowStepDefinition.ConfigurationScope<K>.() -> Unit = {},
    ): R {
        return step(
            stepDefinition = object : FlowStepDefinition<K, R>() {
                override val keyWithMetadata = key
                override val result = R::class

                init { ConfigurationScope<K>(this).block() }
            },
        )
    }

    /**
     * Vararg form of [async]. A single [List] argument is used as the list of dependencies.
     */
    @Suppress("NOTHING_TO_INLINE") // required for using block's name as an identifier
    public inline fun <T> async(
        vararg dependsOn: Any?,
        noinline block: suspend () -> T,
    ): T {
        if (dependsOn.size == 1 && dependsOn[0] is List<*>) {
            return async(dependsOn = dependsOn[0] as List<Any?>, block = block)
        }
        return async(dependsOn.toList(), block)
    }

    /**
     * Runs [block] as a step of the flow. The flow stops at the [async] call until [block]
     * returns, then runs the flow lambda again with the result of [block].
     *
     * [async] shows no UI while [block] runs. Use [async] for a fast [block], or when the
     * screen that hosts the flow shows a loading state. Otherwise open a destination that
     * runs the suspending work and shows a loading state.
     *
     * The result of [block] is not saved. After process death the flow runs [block] again.
     *
     * @param dependsOn When the hash of [dependsOn] changes, the flow cancels the running or
     * completed [block] and runs [block] again.
     */
    @AdvancedEnroApi
    @Suppress("NOTHING_TO_INLINE") // required for using block's name as an identifier
    public inline fun <T> async(
        dependsOn: List<Any?> = emptyList(),
        noinline block: suspend () -> T,
    ): T {
        val baseId = callSiteClassName(block::class) ?: "async"
        val count = suspendingSteps.count { it.startsWith(baseId) }
        val stepId = "$baseId@$count"
        suspendingSteps.add(stepId)

        val dependencyHash = dependsOn.hashForDependsOn()

        val existing = resultManager.suspendingResults[stepId]?.let {
            when {
                it.dependsOn != dependencyHash -> {
                    it.job.cancel()
                    it.result.cancel()
                    null
                }

                else -> it
            }
        }
        if (existing != null && !existing.result.isCancelled) {
            if (!existing.result.isCompleted) escape()

            @OptIn(ExperimentalCoroutinesApi::class)
            @Suppress("UNCHECKED_CAST")
            return existing.result.getCompleted() as T
        }

        val deferredResult = coroutineScope.async(start = CoroutineStart.LAZY) {
            block()
        }
        val job = coroutineScope.launch(start = CoroutineStart.LAZY) {
            deferredResult.await()
            flow.update()
        }
        resultManager.suspendingResults[stepId] = FlowResultManager.SuspendingStepResult(
            id = FlowStep.Id<NavigationKey>(stepId),
            result = deferredResult,
            job = job,
            dependsOn = dependencyHash,
        )
        job.start()
        escape()
    }

    @PublishedApi
    @JvmName("stepWithMetadata")
    internal inline fun <reified T : NavigationKey, R : Any> step(
        stepDefinition: FlowStepDefinition<T, R>,
    ): R {
        val step = stepDefinition.buildStep(this)
        val defaultResult = stepDefinition.defaultResult
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
