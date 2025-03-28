package dev.enro.test

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.closeWithResult
import dev.enro.core.container.push
import dev.enro.core.container.setBackstack
import dev.enro.core.forward
import dev.enro.core.onActiveContainer
import dev.enro.core.onContainer
import dev.enro.core.onParentContainer
import dev.enro.core.result.flows.registerForFlowResult
import dev.enro.core.result.registerForNavigationResult
import dev.enro.viewmodel.navigationHandle
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class TestTestKeyWithData(
    val id: String
) : Parcelable, NavigationKey.SupportsPush, NavigationKey.SupportsPresent

val testContainerKey = NavigationContainerKey.FromName("test container")

@Parcelize
class TestResultStringKey(val id: String = UUID.randomUUID().toString()) :
    NavigationKey.SupportsPush.WithResult<String>,
    NavigationKey.SupportsPresent.WithResult<String>

class TestResultStringViewModel : ViewModel() {
    private val navigation by navigationHandle<TestResultStringKey>()

    fun sendResult(result: String) {
        navigation.closeWithResult(result)
    }
}

@Parcelize
class TestResultIntKey : NavigationKey.WithResult<Int>

class TestResultIntViewModel : ViewModel() {
    private val navigation by navigationHandle<TestResultIntKey>()
}

@Parcelize
class TestTestNavigationKey : NavigationKey

class TestTestViewModel : ViewModel() {
    private val navigation by navigationHandle<TestTestNavigationKey> {
        onCloseRequested {
            wasCloseRequested = true
            close()
        }
    }

    var wasCloseRequested: Boolean = false

    var stringOneResult: String? = null
    var stringTwoResult: String? = null
    var intOneResult: Int? = null
    var intTwoResult: Int? = null

    private val stringOne by registerForNavigationResult<String> {
        stringOneResult = it
        openStringTwo()
    }

    private val stringTwo by registerForNavigationResult<String> {
        stringTwoResult = it
        openIntOne()
    }

    private val intOne by registerForNavigationResult<Int> {
        intOneResult = it
        openIntTwo()
    }

    private val intTwo by registerForNavigationResult<Int> {
        intTwoResult = it
        navigation.close()
    }

    fun openStringOne() {
        stringOne.open(TestResultStringKey())
    }

    fun openStringTwo() {
        stringTwo.open(TestResultStringKey())
    }

    fun openIntOne() {
        intOne.open(TestResultIntKey())
    }

    fun openIntTwo() {
        intTwo.open(TestResultIntKey())
    }

    fun parentContainerOperation(id: String) {
        navigation.onParentContainer {
            setBackstack {
                it.push(TestTestKeyWithData(id))
            }
        }
    }

    fun activeContainerOperation(id: String) {
        navigation.onActiveContainer {
            setBackstack {
                it.push(TestTestKeyWithData(id))
            }
        }
    }

    fun specificContainerOperation(id: String) {
        navigation.onContainer(testContainerKey) {
            setBackstack {
                it.push(TestTestKeyWithData(id))
            }
        }
    }

    fun forwardToTestWithData(id: String) {
        navigation.forward(TestTestKeyWithData(id))
    }
}

@Parcelize
object FlowTestKey : NavigationKey.SupportsPresent.WithResult<FlowData>

data class FlowData(
    val first: String,
    val second: String,
    val bottomSheet: String,
    val third: String,
)

@OptIn(ExperimentalEnroApi::class)
class FlowViewModel() : ViewModel() {
    val navigation by navigationHandle<FlowTestKey>()
    val flow by registerForFlowResult(
        savedStateHandle = SavedStateHandle(),
        flow = {
            val first = push { TestResultStringKey("first") }
            val second = push { TestResultStringKey("second") }
            val bottomSheet = present {
                dependsOn("second")
                TestResultStringKey("bottomSheet")
            }
            val third = push { TestResultStringKey("third") }
            FlowData(
                first = first,
                second = second,
                bottomSheet = bottomSheet,
                third = third,
            )
        },
        onCompleted = {
            navigation.closeWithResult(it)
        }
    )
}