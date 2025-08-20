package dev.enro.result.flow

import androidx.lifecycle.ViewModel
import androidx.savedstate.savedState
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.complete
import dev.enro.interceptor.builder.navigationInterceptor
import dev.enro.navigationHandle
import dev.enro.result.NavigationResultChannel
import dev.enro.test.NavigationContextFixtures
import dev.enro.test.assertCompleted
import dev.enro.test.putNavigationHandleForViewModel
import dev.enro.test.runEnroTest
import dev.enro.ui.EmptyBehavior
import dev.enro.ui.NavigationContainerState
import dev.enro.ui.decorators.NavigationSavedStateHolder
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertTrue

class ResultFlowTest {

    @Test
    fun test() = runEnroTest {
        val testNavigationHandle = putNavigationHandleForViewModel<ResultFlowViewModel, ResultFlowDestination>(ResultFlowDestination())
        val viewModel = ResultFlowViewModel()

        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        val containerState = NavigationContainerState(
            container = container,
            emptyBehavior = EmptyBehavior.preventEmpty(),
            context = containerContext,
            savedStateHolder = NavigationSavedStateHolder(savedState()),
        )
        container.addInterceptor(
            navigationInterceptor {
                onClosed<NavigationKey> {
                    val stepId = instance.metadata.get(FlowStep.FlowStepIdKey)
                    if (stepId != null && !isSilent) {
                        viewModel.flow.onStepClosed(stepId)
                    }
                    continueWithClose()
                }
                onCompleted<NavigationKey> {
                    val stepId = instance.metadata.get(FlowStep.FlowStepIdKey)
                        ?: instance.metadata.get(NavigationResultChannel.ResultIdKey)?.let { resultId ->
                            viewModel.flow.getSteps()
                                .firstOrNull { it.stepId == resultId.resultId }
                                ?.stepId
                        }
                    if (stepId == null) continueWithComplete()
                    cancelAnd {
                        viewModel.flow.onStepCompleted(stepId, data ?: Unit)
                        viewModel.flow.update()
                    }
                }
            }
        )
        viewModel.flow.container = containerState

        val first = container.backstack[0] as NavigationKey.Instance<RequestString>
        assertTrue {
            first.key.name == "First"
        }
        container.execute(containerContext, NavigationOperation.Complete(first, "1"))

        val second = container.backstack[1] as NavigationKey.Instance<RequestString>
        assertTrue {
            second.key.name == "Second"
        }
        container.execute(containerContext,  NavigationOperation.Complete(second, "2"))

        val third = container.backstack[2] as NavigationKey.Instance<RequestString>
        assertTrue {
            third.key.name == "Third"
        }
        container.execute(containerContext,  NavigationOperation.Complete(third, "3"))

        testNavigationHandle.assertCompleted("""
            First: 1
            Second: 2
            Third: 3
        """.trimIndent())
    }

    class ResultFlowViewModel : ViewModel() {
        private val navigation by navigationHandle<ResultFlowDestination>()
        val flow by registerForFlowResult(
            flow = {
                val first = open {
                    RequestString("First")
                }
                val second = open {
                    RequestString("Second")
                }
                val third = open {
                    RequestString("Third")
                }

                return@registerForFlowResult """
                    First: $first
                    Second: $second
                    Third: $third
                """.trimIndent()
            },
            onCompleted = { result ->
                navigation.complete(result)
            }
        )
    }

    @Serializable
    class ResultFlowDestination : NavigationKey.WithResult<String>

    @Serializable
    class RequestString(
        val name: String,
    ) : NavigationKey.WithResult<String>
}