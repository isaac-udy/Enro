package dev.enro.result.flow

import androidx.lifecycle.ViewModel
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.test.assertCompleted
import dev.enro.test.fixtures.NavigationContainerFixtures
import dev.enro.test.putNavigationHandleForViewModel
import dev.enro.test.runEnroTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertTrue

class ResultFlowTest {

    @Test
    fun test() = runEnroTest {
        val testNavigationHandle =
            putNavigationHandleForViewModel<ResultFlowViewModel, ResultFlowDestination>(
                ResultFlowDestination()
            )
        val viewModel = ResultFlowViewModel()
        val container = NavigationContainerFixtures.createForFlow(viewModel.flow)

        val first = container.backstack[0] as NavigationKey.Instance<RequestString>
        assertTrue {
            first.key.name == "First"
        }
        container.execute(NavigationOperation.Complete(first, "1"))

        val second = container.backstack[1] as NavigationKey.Instance<RequestString>
        assertTrue {
            second.key.name == "Second"
        }
        container.execute(NavigationOperation.Complete(second, "2"))

        val third = container.backstack[2] as NavigationKey.Instance<RequestString>
        assertTrue {
            third.key.name == "Third"
        }
        container.execute(NavigationOperation.Complete(third, "3"))

        testNavigationHandle.assertCompleted(
            """
            First: 1
            Second: 2
            Third: 3
        """.trimIndent()
        )
    }

    class ResultFlowViewModel : ViewModel() {
        private val navigation by navigationHandle<ResultFlowDestination>()
        val flow by registerForFlowResult(
            flow = {
                val first = open(
                    RequestString("First")
                )
                val second = open(
                    RequestString("Second")
                )
                val third = open(
                    RequestString("Third")
                )

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