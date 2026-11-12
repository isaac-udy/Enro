package dev.enro.result.flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.enro.getNavigationHandle
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

/**
 * Creates a [NavigationFlow] scoped to this [ViewModel]. A destination can register one
 * [NavigationFlow]: a registration from a second property throws [IllegalArgumentException].
 *
 * The [NavigationFlow] runs [flow] once for each update, so [flow] must not have side effects.
 * The [NavigationFlow] calls [onCompleted] with the value [flow] returns.
 */
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
