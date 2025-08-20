package dev.enro.core.result.flows

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dev.enro.result.flow.registerForFlowResult as realRegisterForFlowResult
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty


public typealias NavigationFlow<T> = dev.enro.result.flow.NavigationFlow<T>


@Deprecated(
    message = "registerForFlowResult has moved to dev.enro.result.flow.registerForFlowResult. Navigation flows have changed and need to be updated following the migration guide.",
    // isManuallyStarted is not a valid parameter anymore
    level = DeprecationLevel.ERROR
)
public fun <T> ViewModel.registerForFlowResult(
    savedStateHandle: SavedStateHandle,
    isManuallyStarted: Boolean,
    flow: NavigationFlowScope.() -> T,
    onCompleted: (T) -> Unit,
): PropertyDelegateProvider<ViewModel, ReadOnlyProperty<ViewModel, NavigationFlow<T>>> {
    return realRegisterForFlowResult(
        flow = {
            val compatScope = NavigationFlowScope(this)
            compatScope.flow()
        },
        onCompleted = onCompleted
    )
}

@Deprecated(
    message = "registerForFlowResult has moved to dev.enro.result.flow.registerForFlowResult. Navigation flows have changed and need to be updated following the migration guide.",
    level = DeprecationLevel.WARNING
)
public fun <T> ViewModel.registerForFlowResult(
    savedStateHandle: SavedStateHandle,
    flow: NavigationFlowScope.() -> T,
    onCompleted: (T) -> Unit,
): PropertyDelegateProvider<ViewModel, ReadOnlyProperty<ViewModel, NavigationFlow<T>>> {
    return realRegisterForFlowResult(
        flow = {
            val compatScope = NavigationFlowScope(this)
            compatScope.flow()
        },
        onCompleted = onCompleted
    )
}

@Deprecated(
    message = "registerForFlowResult has moved to dev.enro.result.flow.registerForFlowResult. Navigation flows have changed and need to be updated following the migration guide.",
    // isManuallyStarted is not a valid parameter anymore
    level = DeprecationLevel.ERROR
)
public fun <T> ViewModel.registerForFlowResult(
    isManuallyStarted: Boolean,
    flow: NavigationFlowScope.() -> T,
    onCompleted: (T) -> Unit,
): PropertyDelegateProvider<ViewModel, ReadOnlyProperty<ViewModel, NavigationFlow<T>>> {
    return realRegisterForFlowResult(
        flow = {
            val compatScope = NavigationFlowScope(this)
            compatScope.flow()
        },
        onCompleted = onCompleted
    )
}

@Deprecated(
    message = "registerForFlowResult has moved to dev.enro.result.flow.registerForFlowResult. Navigation flows have changed and need to be updated following the migration guide.",
    level = DeprecationLevel.WARNING
)
public fun <T> ViewModel.registerForFlowResult(
    flow: NavigationFlowScope.() -> T,
    onCompleted: (T) -> Unit,
): PropertyDelegateProvider<ViewModel, ReadOnlyProperty<ViewModel, NavigationFlow<T>>> {
    return realRegisterForFlowResult(
        flow = {
            val compatScope = NavigationFlowScope(this)
            compatScope.flow()
        },
        onCompleted = onCompleted
    )
}

