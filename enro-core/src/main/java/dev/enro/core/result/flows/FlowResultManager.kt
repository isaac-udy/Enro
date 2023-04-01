package dev.enro.core.result.flows

import android.os.Bundle
import androidx.compose.runtime.mutableStateMapOf
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import dev.enro.extensions.getParcelableListCompat
import dev.enro.extensions.isSaveableInBundle

@PublishedApi
internal class FlowResultManager(
    savedStateHandle: SavedStateHandle?
) {
    val results = mutableStateMapOf<String, CompletedFlowStep>().apply {
        savedStateHandle ?: return@apply
        val bundle = savedStateHandle.get<Bundle>(RESULTS_KEY) ?: return@apply
        val savedList = bundle.getParcelableListCompat<CompletedFlowStep>(RESULTS_KEY).orEmpty()
        val savedMap = savedList.associateBy { it.stepId }
        putAll(savedMap)
    }

    init {
        savedStateHandle?.setSavedStateProvider(RESULTS_KEY) {
            val saveable = results.values.filter { it.result.isSaveableInBundle() }
            bundleOf(RESULTS_KEY to ArrayList(saveable))
        }
    }

    companion object {
        private const val RESULTS_KEY = "FlowResultManager.RESULTS_KEY"
    }
}