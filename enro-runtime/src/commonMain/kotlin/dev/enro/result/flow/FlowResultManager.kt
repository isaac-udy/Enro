package dev.enro.result.flow

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.SavedStateHandle
import androidx.savedstate.SavedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import dev.enro.EnroController
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.displayName
import dev.enro.platform.EnroLog
import dev.enro.result.flow.FlowResultManager.FlowStepResult
import dev.enro.serialization.unwrapForSerialization
import dev.enro.serialization.wrapForSerialization
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.uuid.Uuid

public class FlowResultManager private constructor(
    private val savedStateHandle: SavedStateHandle,
) {
    private val results = mutableStateMapOf<FlowStep.Id<*>, FlowStepResult<Any>>()
        .also { results ->
            savedStateHandle.setSavedStateProvider("results") {
                encodeFlowResults(results)
            }
            val savedResults = savedStateHandle.get<SavedState>("results") ?: return@also
            val restoredResults = decodeFlowResults(savedResults)
            results.putAll(restoredResults)
        }

    private val defaultsInitialised = mutableSetOf<FlowStep.Id<*>>()
        .also { defaultsInitialised ->
            savedStateHandle.setSavedStateProvider("defaults") {
                encodeToSavedState(
                    serializer = ListSerializer(String.serializer()),
                    value = defaultsInitialised.toList().map { it.value },
                    configuration = EnroController.savedStateConfiguration,
                )
            }
            val savedDefaults = savedStateHandle.get<SavedState>("defaults") ?: return@also
            val restoredDefaults = decodeFromSavedState(
                savedState = savedDefaults,
                deserializer = ListSerializer(String.serializer()),
                configuration = EnroController.savedStateConfiguration,
            )
            defaultsInitialised.addAll(restoredDefaults.map { FlowStep.Id<NavigationKey>(it) })
        }

    @PublishedApi
    internal val suspendingResults: SnapshotStateMap<String, SuspendingStepResult> = mutableStateMapOf()

    public fun <T : Any> get(step: FlowStep<T>): T? {
        val completedStep = results[step.id] ?: return null
        val result = completedStep.result as? T
        if (step.dependsOn != completedStep.dependsOn) {
            results.remove(step.id)
            return null
        }
        return result
    }

    public fun <T : Any> set(step: FlowStep<T>, result: T) {
        results[step.id] = FlowStepResult(
            id = step.id,
            result = result,
            dependsOn = step.dependsOn,
            instanceId = Uuid.random().toString(),
        )
    }

    public fun <T : Any> setDefault(step: FlowStep<T>, result: T) {
        if (defaultsInitialised.contains(step.id)) return
        defaultsInitialised.add(step.id)
        set(step, result)
    }

    public fun clear(id: FlowStep.Id<*>) {
        results.remove(id)
    }

    public fun getResultInstanceId(id: FlowStep.Id<*>): String? {
        return results[id]?.instanceId
    }

    @Serializable
    @PublishedApi
    internal class FlowStepResult<out T : Any>(
        val id: FlowStep.Id<*>,
        val result: T,
        val dependsOn: Long,
        // A random UUID assigned each time a result is set. FlowStepOptions.AlwaysAfterPrevious
        // compares instanceId to detect a new result that is equal to the earlier result.
        val instanceId: String,
    )

    @PublishedApi
    internal class SuspendingStepResult(
        val id: FlowStep.Id<*>,
        val result: Deferred<Any?>,
        val job: Job,
        val dependsOn: Long,
    )

    public companion object {
        internal object FlowResultManagerKey : NavigationKey.TransientMetadataKey<FlowResultManager?>(
            default = null,
            name = "dev.enro.result.flow.FlowResultManager.Companion.FlowResultManagerKey",
        )

        public fun create(
            navigationHandle: NavigationHandle<*>,
        ): FlowResultManager {
            return navigationHandle.instance.metadata.get(FlowResultManagerKey)
                ?: FlowResultManager(navigationHandle.savedStateHandle).also {
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

private fun encodeFlowResults(
    results: Map<FlowStep.Id<*>, FlowResultManager.FlowStepResult<Any>>,
): SavedState {
    // TODO: add an option that sets filterMissingSerializers. Filtering a result should also clear the
    //  results of each later step, so that a restored flow shows the step with the filtered result.
    val filterMissingSerializers = false
    val wrappedResults = results.values
        .map {
            FlowStepResult(
                id = it.id,
                result = it.result.wrapForSerialization(),
                dependsOn = it.dependsOn,
                instanceId = it.instanceId,
            )
        }
        .let { wrappedResults ->
            if (!filterMissingSerializers) return@let wrappedResults
            wrappedResults.filter {
                val serializer =
                    EnroController.savedStateConfiguration.serializersModule.getPolymorphic(Any::class, it.result)
                if (serializer == null) {
                    EnroLog.error("Could not find serializer for ${it.result::class.displayName}, result will not be saved/restored in navigation flow")
                }
                return@filter serializer != null
            }
        }

    return encodeToSavedState(
        serializer = ListSerializer(FlowStepResult.serializer(PolymorphicSerializer(Any::class))),
        value = wrappedResults,
        configuration = EnroController.savedStateConfiguration,
    )
}

private fun decodeFlowResults(savedState: SavedState): Map<FlowStep.Id<*>, FlowStepResult<Any>> {
    val restoredResults = decodeFromSavedState(
        savedState = savedState,
        deserializer = ListSerializer(FlowStepResult.serializer(PolymorphicSerializer(Any::class))),
        configuration = EnroController.savedStateConfiguration,
    )
    return restoredResults.associate {
        it.id to FlowStepResult(
            id = it.id,
            result = it.result.unwrapForSerialization(),
            dependsOn = it.dependsOn,
            instanceId = it.instanceId,
        )
    }
}
