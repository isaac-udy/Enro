package dev.enro.core.result.flows

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.SavedStateHandle
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.savedState
import androidx.savedstate.serialization.decodeFromSavedState
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.TypedNavigationHandle
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.usecase.extras
import dev.enro.core.getParentNavigationHandle
import dev.enro.extensions.isSaveable
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.collections.set

public class FlowResultManager private constructor(
    savedStateHandle: SavedStateHandle?
) {
    private val results = mutableStateMapOf<String, FlowStepResult>().apply {
        savedStateHandle ?: return@apply
        val savedState = savedStateHandle.get<SavedState>(SAVED_STATE_KEY) ?: return@apply
        val savedList = savedState.read {

            getSavedStateListOrNull(RESULTS_KEY)
                .orEmpty()
                .map { decodeFromSavedState<FlowStepResult>(it, NavigationController.savedStateConfiguration) }
        }
        val savedMap = savedList.associateBy { it.stepId }
        putAll(savedMap)
    }

    @PublishedApi
    internal val suspendingResults: SnapshotStateMap<String, SuspendingStepResult> = mutableStateMapOf()

    private val defaultsInitialised = mutableSetOf<String>().apply {
        savedStateHandle ?: return@apply
        val savedState = savedStateHandle.get<SavedState>(SAVED_STATE_KEY) ?: return@apply
        val saved = savedState.read {
            getStringListOrNull(DEFAULT_SET_KEY).orEmpty()
        }
        addAll(saved)
    }

    public fun <T: Any> get(step: FlowStep<T>) : T? {
        val completedStep = results[step.stepId] ?: return null
        val result = completedStep.result as? T
        if (step.dependsOn != completedStep.dependsOn) {
            results.remove(step.stepId)
            return null
        }
        return result
    }

    public fun <T: Any> set(step: FlowStep<T>, result: T) {
        results[step.stepId] = FlowStepResult(
            stepId = step.stepId,
            result = result,
            dependsOn = step.dependsOn,
        )
    }

    public fun <T: Any> setDefault(step: FlowStep<T>, result: T) {
        if (defaultsInitialised.contains(step.stepId)) return
        defaultsInitialised.add(step.stepId)
        set(step, result)
    }

    public fun clear(step: FlowStep<out Any>) {
        results.remove(step.stepId)
    }

    init {
        savedStateHandle?.setSavedStateProvider(SAVED_STATE_KEY) {
            val resultsToSave = results.values.filter { it.result.isSaveable() }
            val defaultsToSave = defaultsInitialised
            savedState(
                mapOf(
                    RESULTS_KEY to ArrayList(resultsToSave),
                    DEFAULT_SET_KEY to ArrayList(defaultsToSave),
                )
            )
        }
    }

    @Serializable
    @PublishedApi
    internal class FlowStepResult (
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
        private const val SAVED_STATE_KEY = "FlowResultManager.RESULTS_KEY"
        private const val RESULTS_KEY = "FlowResultManager.RESULTS_KEY"
        private const val DEFAULT_SET_KEY = "FlowResultManager.DEFAULT_SET_KEY"

        private const val NAVIGATION_HANDLE_EXTRA = "FlowResultManager.NAVIGATION_HANDLE_EXTRA"

        public fun create(
            navigationHandle: NavigationHandle,
            savedStateHandle: SavedStateHandle,
        ) : FlowResultManager {
            return navigationHandle.extras.getOrPut(NAVIGATION_HANDLE_EXTRA) {
                FlowResultManager(savedStateHandle)
            } as FlowResultManager
        }

        public fun get(
            navigationHandle: NavigationHandle,
        ) : FlowResultManager? {
            return navigationHandle.extras[NAVIGATION_HANDLE_EXTRA] as? FlowResultManager
        }
    }
}

public fun <T: Any> TypedNavigationHandle<out NavigationKey.WithResult<T>>.getFlowResult(): T? {
    val step = instruction.resultKey
    if (step == null || step !is FlowStep<*>) return null
    val parentNavigationHandle = getParentNavigationHandle() ?: return null
    val resultManager = FlowResultManager.get(parentNavigationHandle) ?: return null
    return resultManager.get(step) as? T
}

@Composable
public fun <T: Any> TypedNavigationHandle<out NavigationKey.WithResult<T>>.rememberFlowResult(): T? {
    val step = instruction.resultKey
    if (step == null || step !is FlowStep<*>) return null

    val parentNavigationHandle = remember(this) {
        getParentNavigationHandle()
    } ?: return null

    val resultManager = remember(parentNavigationHandle) {
        FlowResultManager.get(parentNavigationHandle)
    } ?: return null

    return resultManager.get(step) as? T
}
