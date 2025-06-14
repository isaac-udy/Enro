package dev.enro.result.flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.getNavigationHandle
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

/**
 * This method creates a NavigationFlow in the scope of a ViewModel. There can only be one NavigationFlow created within each
 * NavigationDestination. The [flow] lambda will be invoked multiple times over the lifecycle of the NavigationFlow, and should
 * generally not cause external side effects. The [onCompleted] lambda will be invoked when the flow completes and returns a
 * result.
 */
@ExperimentalEnroApi
public fun <T> ViewModel.registerForFlowResult(
    flow: NavigationFlowScope.() -> T,
    onCompleted: (T) -> Unit,
): PropertyDelegateProvider<ViewModel, ReadOnlyProperty<ViewModel, NavigationFlow<T>>> {
    return PropertyDelegateProvider { thisRef, property ->
        val navigation = thisRef.getNavigationHandle()
        val resultFlowId = property.name
        val boundResultFlowId = navigation.instance.metadata.get(NavigationFlow.Companion.ResultFlowIdKey)
        require(boundResultFlowId == null || boundResultFlowId == resultFlowId) {
            "Only one registerForFlowResult can be created per NavigationHandle. Found an existing result flow for $boundResultFlowId."
        }
        navigation.instance.metadata.set(NavigationFlow.Companion.ResultFlowIdKey, resultFlowId)
        val navigationFlow = NavigationFlow(
            reference = NavigationFlowReference(resultFlowId),
            navigationHandle = navigation,
            coroutineScope = thisRef.viewModelScope,
            flow = flow,
            onCompleted = onCompleted,
        )
        navigation.instance.metadata.set(NavigationFlow.Companion.ResultFlowKey, navigationFlow)
        ReadOnlyProperty { _, _ -> navigationFlow }
    }
}