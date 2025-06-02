package dev.enro3.result.flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro3.NavigationHandle
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

/**
 * This method creates a NavigationFlow in the scope of a ViewModel. There can only be one NavigationFlow created within each
 * NavigationDestination. The [flow] lambda will be invoked multiple times over the lifecycle of the NavigationFlow, and should
 * generally not cause external side effects. The [onCompleted] lambda will be invoked when the flow completes and returns a
 * result.
 *
 * If [isManuallyStarted] is false, [NavigationFlow.update] is triggered automatically as part of this function,
 * and you do not need to manually call update to begin the flow. This is the default behavior.
 *
 * If [isManuallyStarted] is true, you will need to call [NavigationFlow.update] to trigger the initial update of the flow,
 * which will then trigger the flow to continue.
 */
@ExperimentalEnroApi
public fun <T> ViewModel.registerForFlowResult(
    navigationHandle: NavigationHandle<*>,
    isManuallyStarted: Boolean = false,
    flow: NavigationFlowScope.() -> T,
    onCompleted: (T) -> Unit,
): PropertyDelegateProvider<ViewModel, ReadOnlyProperty<ViewModel, NavigationFlow<T>>> {
    return PropertyDelegateProvider { thisRef, property ->
        val resultFlowId = property.name
        val boundResultFlowId = navigationHandle.instance.metadata.get(NavigationFlow.Companion.ResultFlowIdKey)
        require(boundResultFlowId == null || boundResultFlowId == resultFlowId) {
            "Only one registerForFlowResult can be created per NavigationHandle. Found an existing result flow for $boundResultFlowId."
        }
        navigationHandle.instance.metadata.set(NavigationFlow.Companion.ResultFlowIdKey, resultFlowId)

        val resultManager = FlowResultManager.create(navigationHandle)
        val navigationFlow = NavigationFlow(
            reference = NavigationFlowReference(resultFlowId),
            resultManager = resultManager,
            coroutineScope = thisRef.viewModelScope,
            flow = flow,
            onCompleted = onCompleted,
        )
        navigationHandle.instance.metadata.set(NavigationFlow.Companion.ResultFlowKey, navigationFlow)
        if (!isManuallyStarted) {
            navigationFlow.update()
        }
        ReadOnlyProperty { _, _ -> navigationFlow }
    }
}