@file:Suppress("DEPRECATION")
package dev.enro.result

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ActivityScenario
import dev.enro.DefaultActivity
import dev.enro.TestFragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.closeWithResult
import dev.enro.core.forward
import dev.enro.core.result.registerForNavigationResult
import dev.enro.expectFragment
import dev.enro.getNavigationHandle
import dev.enro.viewmodel.enroViewModels
import dev.enro.viewmodel.navigationHandle
import dev.enro.waitFor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class ViewModelResultTests {
    @get:Rule
    val rule = kotlin.run {
        // It appears there's a false positive leak on SDK 23 with this test class,
        // so we're going to ignore the leak rule for SDK 23
        if (Build.VERSION.SDK_INT == 23) return@run TestRule { base, _ -> base }
        DetectLeaksAfterTestSuccess()
    }

    @Test
    fun givenOrchestratedResultFlowManagedByViewModels_whenOrchestratedResultFlowExecutes_thenResultsAreReceivedCorrectly() {
        ActivityScenario.launch(DefaultActivity::class.java)
            .getNavigationHandle<NavigationKey>()
            .forward(OrchestratorKey())

        val viewModel = expectFragment<OrchestratorFragment>()
            .viewModel

        waitFor { "FirstStep -> SecondStep(SecondStepNested)" == viewModel.currentResult }
    }
}


@Parcelize
class OrchestratorKey : Parcelable, NavigationKey

class OrchestratorViewModel : ViewModel() {
    var currentResult = ""

    val navigation by navigationHandle<NavigationKey>()
    val resultOne by registerForNavigationResult<String> {
        currentResult = it
        resultTwo.open(SecondStepKey())
    }
    val resultTwo by registerForNavigationResult<String> {
        currentResult = "$currentResult -> $it"
    }

    init {
        viewModelScope.launch {
            delay(100)
            resultOne.open(FirstStepKey())
        }
    }
}

@NavigationDestination(OrchestratorKey::class)
class OrchestratorFragment : TestFragment() {
    val viewModel by enroViewModels<OrchestratorViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.hashCode()
    }
}

@Parcelize
class FirstStepKey : Parcelable, NavigationKey.WithResult<String>

class FirstStepViewModel : ViewModel() {
    private val navigation by navigationHandle<FirstStepKey>()
    init {
        viewModelScope.launch {
            delay(100)
            navigation.closeWithResult("FirstStep")
        }
    }
}

@NavigationDestination(FirstStepKey::class)
class FirstStepFragment : TestFragment() {
    private val viewModel by enroViewModels<FirstStepViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.hashCode()
    }
}

@Parcelize
class SecondStepKey : Parcelable, NavigationKey.WithResult<String>

class SecondStepViewModel : ViewModel() {
    private val navigation by navigationHandle<SecondStepKey>()
    private val nested by registerForNavigationResult<String> {
        navigation.closeWithResult("SecondStep($it)")
    }
    init {
        viewModelScope.launch {
            delay(100)
            nested.open(SecondStepNestedKey())
        }
    }
}

@NavigationDestination(SecondStepKey::class)
class SecondStepFragment : TestFragment() {
    private val viewModel by enroViewModels<SecondStepViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.hashCode()
    }
}


@Parcelize
class SecondStepNestedKey : Parcelable, NavigationKey.WithResult<String>

class SecondStepNestedViewModel : ViewModel() {
    private val navigation by navigationHandle<SecondStepNestedKey>()
    init {
        viewModelScope.launch {
            delay(100)
            navigation.closeWithResult("SecondStepNested")
        }
    }
}

@NavigationDestination(SecondStepNestedKey::class)
class SecondStepNestedFragment : TestFragment() {
    private val viewModel by enroViewModels<SecondStepNestedViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.hashCode()
    }
}