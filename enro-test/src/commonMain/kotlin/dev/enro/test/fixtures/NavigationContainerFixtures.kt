package dev.enro.test.fixtures

import androidx.savedstate.savedState
import dev.enro.NavigationBackstack
import dev.enro.NavigationContainer
import dev.enro.NavigationContainerFilter
import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.acceptAll
import dev.enro.backstackOf
import dev.enro.context.ContainerContext
import dev.enro.context.DestinationContext
import dev.enro.context.RootContext
import dev.enro.emptyBackstack
import dev.enro.interceptor.NavigationInterceptor
import dev.enro.interceptor.NoOpNavigationInterceptor
import dev.enro.interceptor.builder.navigationInterceptor
import dev.enro.result.NavigationResultChannel
import dev.enro.result.flow.NavigationFlow
import dev.enro.result.flow.flowStepId
import dev.enro.test.EnroTest
import dev.enro.ui.EmptyBehavior
import dev.enro.ui.NavigationContainerState
import dev.enro.ui.decorators.NavigationSavedStateHolder
import kotlin.uuid.Uuid

object NavigationContainerFixtures {
    internal object ContainerFixtureKey : NavigationKey.TransientMetadataKey<NavigationContainerState?>(null)

    fun create(
        parentContext: NavigationContext = NavigationContextFixtures.createRootContext(),
        key: NavigationContainer.Key = NavigationContainer.Key("TestNavigationContainer@${Uuid.random()}"),
        backstack: NavigationBackstack = backstackOf(),
        emptyBehavior: EmptyBehavior = EmptyBehavior.preventEmpty(),
        interceptor: NavigationInterceptor = NoOpNavigationInterceptor,
        filter: NavigationContainerFilter = acceptAll(),
    ): NavigationContainerState {
        require(parentContext is RootContext || parentContext is DestinationContext<*>) {
            "NavigationContainer can only be used within a RootContext or DestinationContext"
        }
        val controller = requireNotNull(EnroTest.getCurrentNavigationController()) {
            "EnroController instance is not initialized"
        }
        val container = NavigationContainer(
            key = key,
            controller = controller,
            backstack = backstack,
        )
        container.setFilter(filter)
        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        container.addEmptyInterceptor(emptyBehavior.interceptor)
        container.addInterceptor(interceptor)

        val context = ContainerContext(
            container = container,
            parent = parentContext,
        )

        val savedState = NavigationSavedStateHolder(savedState())
        val containerState = NavigationContainerState(
            container = container,
            emptyBehavior = emptyBehavior,
            context = context,
            savedStateHolder = savedState,
        )
        container.addInterceptor(
            navigationInterceptor {
                onOpened<NavigationKey> {
                    instance.metadata.set(ContainerFixtureKey, containerState)
                    continueWithOpen()
                }
            }
        )
        return containerState
    }

    fun createForFlow(
        flow: NavigationFlow<*>
    ): NavigationContainerState {
        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        return create(
            key = NavigationContainer.Key("TestNavigationFlow"),
            backstack = emptyBackstack(),
            filter = run {
                NavigationContainerFilter(
                    fromChildrenOnly = true,
                    block = { true },
                )
            },
            interceptor = navigationInterceptor {
                onClosed<NavigationKey> {
                    val stepId = instance.flowStepId
                    if (stepId != null && !isSilent) {
                        flow.onStepClosed(stepId)
                    }
                    continueWithClose()
                }
                onCompleted<NavigationKey> {
                    val stepId = instance.flowStepId
                        ?: instance.metadata.get(NavigationResultChannel.ResultIdKey)
                            ?.let { resultId ->
                                flow.getSteps()
                                    .firstOrNull { it.id.value == resultId.resultId }
                                    ?.id
                            }
                    if (stepId == null) continueWithComplete()
                    cancelAnd {
                        flow.onStepCompleted(stepId, data ?: Unit)
                        flow.update()
                    }
                }
            }
        ).also {
            val state = it
            flow.container = state
        }
    }
}