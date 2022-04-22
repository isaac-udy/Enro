package dev.enro.test

import androidx.lifecycle.ViewModel
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.result.registerForNavigationResult
import dev.enro.viewmodel.navigationHandle
import kotlinx.parcelize.Parcelize


@Parcelize
class TestTestNavigationKey : NavigationKey

@Parcelize
class TestResultStringKey : NavigationKey.WithResult<String>

@Parcelize
class TestResultIntKey : NavigationKey.WithResult<Int>

class TestTestViewModel : ViewModel() {
    private val navigation by navigationHandle<TestTestNavigationKey>()

    var stringOneResult: String? = null
    var stringTwoResult: String? = null
    var intOneResult: Int? = null
    var intTwoResult: Int? = null

    private val stringOne by registerForNavigationResult<String>(navigation) {
        stringOneResult = it
        openStringTwo()
    }

    private val stringTwo by registerForNavigationResult<String>(navigation) {
        stringTwoResult = it
        openIntOne()
    }

    private val intOne by registerForNavigationResult<Int>(navigation) {
        intOneResult = it
        openIntTwo()
    }

    private val intTwo by registerForNavigationResult<Int>(navigation) {
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
}