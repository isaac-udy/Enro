package dev.enro.ui.destinations

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.result.flow.NavigationFlowScope
import dev.enro.result.flow.registerForFlowResult
import dev.enro.result.flow.rememberNavigationContainerForFlow
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.navigationDestination
import dev.enro.viewmodel.createEnroViewModel
import kotlin.reflect.KClass

/**
 * Creates a standalone managed flow destination. This destination type allows you to define
 * a multi-step flow as a single destination that automatically manages its own lifecycle.
 *
 * @param keyType The navigation key type for this flow
 * @param flow The flow definition that describes the steps and logic
 * @param metadata Additional metadata for the destination
 */
@ExperimentalEnroApi
public inline fun <reified T : NavigationKey, R : Any> managedFlowDestination(
    noinline flow: NavigationFlowScope.() -> R,
    noinline onCompleted: ManagedFlowDestinationScope<T>.(R) -> Unit,
): NavigationDestinationProvider<T> {
    return managedFlowDestination(
        keyType = T::class,
        flow = flow,
        onCompleted = onCompleted
    )
}

@ExperimentalEnroApi
public inline fun <reified T : NavigationKey.WithResult<R>, R : Any> managedFlowDestination(
    noinline flow: NavigationFlowScope.() -> R,
): NavigationDestinationProvider<T> {
    return managedFlowDestination(
        keyType = T::class,
        flow = flow,
        onCompleted = { navigation.complete(it) }
    )
}


/**
 * Creates a standalone managed flow destination. This destination type allows you to define
 * a multi-step flow as a single destination that automatically manages its own lifecycle.
 *
 * @param keyType The navigation key type for this flow
 * @param flow The flow definition that describes the steps and logic
 * @param metadata Additional metadata for the destination
 */
@ExperimentalEnroApi
public fun <T : NavigationKey, R : Any> managedFlowDestination(
    keyType: KClass<T>,
    flow: NavigationFlowScope.() -> R,
    onCompleted: ManagedFlowDestinationScope<T>.(R) -> Unit,
): NavigationDestinationProvider<T> {
    return navigationDestination(emptyMap()) {
        ManagedFlowDestinationContent(
            keyType = keyType,
            flow = flow,
            onCompleted = onCompleted,
        )
    }
}

@ExperimentalEnroApi
public fun <T : NavigationKey.WithResult<R>, R : Any> managedFlowDestination(
    keyType: KClass<out NavigationKey.WithResult<R>>,
    flow: NavigationFlowScope.() -> R,
): NavigationDestinationProvider<T> {
    return navigationDestination(emptyMap()) {
        ManagedFlowDestinationContent(
            keyType = keyType,
            flow = flow,
            onCompleted = { navigation.complete(it) },
        )
    }
}

@ExperimentalEnroApi
@Composable
private fun <T : NavigationKey, R : Any> ManagedFlowDestinationContent(
    keyType: KClass<T>,
    flow: NavigationFlowScope.() -> R,
    onCompleted: ManagedFlowDestinationScope<T>.(R) -> Unit,
) {
    val viewModel = viewModel {
        createEnroViewModel {
            ManagedFlowViewModel(
                keyType = keyType,
                flowDefinition = flow,
                onCompleted = onCompleted,
            )
        }
    }
    val container = rememberNavigationContainerForFlow(viewModel.flow)
    NavigationDisplay(
        state = container,
    )
}

@ExperimentalEnroApi
internal class ManagedFlowViewModel<T : NavigationKey, R : Any>(
    keyType: KClass<T>,
    private val flowDefinition: NavigationFlowScope.() -> R,
    private val onCompleted: ManagedFlowDestinationScope<T>.(R) -> Unit,
) : ViewModel() {

    private val navigation by navigationHandle(keyType)

    internal val flow by registerForFlowResult(
        isManuallyStarted = true,
        flow = flowDefinition,
        onCompleted = { result ->
            onCompleted(ManagedFlowDestinationScope(navigation), result)
        }
    )
}

public class ManagedFlowDestinationScope<T : NavigationKey>(
    public val navigation: NavigationHandle<T>,
)