package dev.enro.result.flow

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

public class FlowResultManager private constructor() {
    private val results = mutableStateMapOf<String, FlowStepResult>()

    @PublishedApi
    internal val suspendingResults: SnapshotStateMap<String, SuspendingStepResult> = mutableStateMapOf()

    private val defaultsInitialised = mutableSetOf<String>()

    public fun <T : Any> get(step: FlowStep<T>): T? {
        val completedStep = results[step.stepId] ?: return null
        val result = completedStep.result as? T
        if (step.dependsOn != completedStep.dependsOn) {
            results.remove(step.stepId)
            return null
        }
        return result
    }

    public fun <T : Any> set(step: FlowStep<T>, result: T) {
        results[step.stepId] = FlowStepResult(
            stepId = step.stepId,
            result = result,
            dependsOn = step.dependsOn,
        )
    }

    public fun <T : Any> setDefault(step: FlowStep<T>, result: T) {
        if (defaultsInitialised.contains(step.stepId)) return
        defaultsInitialised.add(step.stepId)
        set(step, result)
    }

    public fun clear(step: FlowStep<out Any>) {
        results.remove(step.stepId)
    }

    @Serializable
    @PublishedApi
    internal class FlowStepResult(
        val stepId: String,
        val result: @Contextual Any,
        val dependsOn: Long,
    )

    @PublishedApi
    internal class SuspendingStepResult(
        val stepId: String,
        val result: Deferred<Any?>,
        val job: Job,
        val dependsOn: Long,
    )

    public companion object {
        private object FlowResultManagerKey : NavigationKey.TransientMetadataKey<FlowResultManager?>(null)

        public fun create(
            navigationHandle: NavigationHandle<*>,
        ): FlowResultManager {
            return navigationHandle.instance.metadata.get(FlowResultManagerKey) ?: FlowResultManager().also {
                navigationHandle.instance.metadata.set(FlowResultManagerKey, it)
            }
        }

        public fun get(
            navigationHandle: NavigationHandle<*>,
        ): FlowResultManager? {
            return navigationHandle.instance.metadata.get(FlowResultManagerKey)
        }
    }
}
