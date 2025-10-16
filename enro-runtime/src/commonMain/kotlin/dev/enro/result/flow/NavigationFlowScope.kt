package dev.enro.result.flow

import dev.enro.NavigationKey
import dev.enro.annotations.AdvancedEnroApi
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
     * See documentation on the other [async] function for more information on how this function works.
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
     * [async] allows the execution of suspending functions during a Navigation Flow. This is a delicate API and should be used
     * with care. In many cases, it would likely provide a  better user experience to implement a NavigationDestination that provides
     * UI to the user (such as a loading spinner) while the suspending function is executing, and then pushing or presenting
     * that Navigation Destination into the flow, rather than using [async], which provides no UI.
     *
     * Suspending steps are never saved when application process death occurs, and will always be re-executed.
     *
     * Examples of when to use [async] include:
     * - Small and fast suspending functions that are known to be quick to execute. For example, fetching a value from a local database.
     * - Waiting for external state, where there is UI provided by the screen that is hosting the flow. For example, using an
     *   [async] call as the first step of a flow, to delay starting the flow while some external state is loaded, where the
     *   screen hosting the flow shows a loading spinner.
     *
     * @param dependsOn A list of objects that this suspending step depends on. If any of these objects change, the suspending
     * function will be re-executed. This is used to ensure that the result of the suspending function is valid.
     *
     * @param block The suspending function to execute.
     */
    @AdvancedEnroApi
    @Suppress("NOTHING_TO_INLINE") // required for using block's name as an identifier
    public inline fun <T> async(
        dependsOn: List<Any?> = emptyList(),
        noinline block: suspend () -> T,
    ): T {
        val baseId = block::class.qualifiedName ?: block::class.toString()
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
