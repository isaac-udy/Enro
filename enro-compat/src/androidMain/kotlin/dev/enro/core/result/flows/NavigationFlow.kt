package dev.enro.core.result.flows

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dev.enro.result.flow.NavigationFlowScope
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty


public typealias NavigationFlow<T> = dev.enro.result.flow.NavigationFlow<T>

@Deprecated(
    message = "registerForFlowResult has moved to dev.enro.result.flow.registerForFlowResult. Navigation flows have changed and need to be updated following the migration guide.",
    level = DeprecationLevel.ERROR
)
public fun <T> ViewModel.registerForFlowResult(
    savedStateHandle: SavedStateHandle,
    isManuallyStarted: Boolean = false,
    flow: NavigationFlowScope.() -> T,
    onCompleted: (T) -> Unit,
): PropertyDelegateProvider<ViewModel, ReadOnlyProperty<ViewModel, NavigationFlow<T>>> {
    error("Update to use dev.enro.result.flow.registerForFlowResult")
}

@Deprecated(
    message = "registerForFlowResult has moved to dev.enro.result.flow.registerForFlowResult. Navigation flows have changed and need to be updated following the migration guide.",
    level = DeprecationLevel.ERROR
)
public fun <T> ViewModel.registerForFlowResult(
    isManuallyStarted: Boolean = false,
    flow: NavigationFlowScope.() -> T,
    onCompleted: (T) -> Unit,
): PropertyDelegateProvider<ViewModel, ReadOnlyProperty<ViewModel, NavigationFlow<T>>> {
    error("Update to use dev.enro.result.flow.registerForFlowResult")
}