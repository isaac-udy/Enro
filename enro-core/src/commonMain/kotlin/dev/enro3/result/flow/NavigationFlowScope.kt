package dev.enro3.result.flow

import dev.enro.annotations.AdvancedEnroApi
import dev.enro3.NavigationKey
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
    internal val steps: MutableList<FlowStep<out Any>> = mutableListOf(),
    @PublishedApi
    internal val suspendingSteps: MutableList<String> = mutableListOf(),
) {
    public inline fun <reified T : Any> open(
        noinline block: FlowStepBuilderScope<T>.() -> NavigationKey.WithResult<T>,
    ): T = step(
        block = object : FlowStepLambda<T> {
            override fun FlowStepBuilderScope<T>.invoke(): NavigationKey.WithResult<T> {
                return block()
            }
        },
    )

    public inline fun <reified T : Any> openWithMetadata(
        noinline block: FlowStepBuilderScope<T>.() -> NavigationKey.WithMetadata<NavigationKey.WithResult<T>>,
    ): T = step(
        block = block,
    )

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
            stepId = stepId,
            result = deferredResult,
            job = job,
            dependsOn = dependencyHash,
        )
        job.start()
        escape()
    }

    @PublishedApi
    internal inline fun <reified T : Any> step(
        block: FlowStepLambda<T>,
    ): T {
        val baseId = block::class.qualifiedName ?: block::class.toString()
        val count = steps.count { it.stepId.startsWith(baseId) }
        val builder = FlowStepBuilder<T>()
        val key = block.run { builder.scope.invoke() }
        val step = builder.build(
            stepId = "$baseId@$count",
            navigationKey = key,
        )
        val defaultResult = builder.getDefaultResult()
        if (defaultResult != null) {
            resultManager.setDefault(step, defaultResult)
        }
        steps.add(step)
        val result = resultManager.get(step)
        return result ?: throw NoResult(step)
    }

    @PublishedApi
    @JvmName("stepWithMetadata")
    internal inline fun <reified T : Any> step(
        noinline block: FlowStepBuilderScope<T>.() -> NavigationKey.WithMetadata<out NavigationKey.WithResult<T>>,
    ): T {
        val baseId = block::class.qualifiedName ?: block::class.toString()
        val count = steps.count { it.stepId.startsWith(baseId) }
        val builder = FlowStepBuilder<T>()
        val key = builder.scope.run(block)
        val step = builder.build(
            stepId = "$baseId@$count",
            navigationKey = key,
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

// TODO create a re-usable identifiable lambda class for more than just flow steps
@PublishedApi
internal interface FlowStepLambda<T : Any> {
    fun FlowStepBuilderScope<T>.invoke(): NavigationKey.WithResult<T>
}