package dev.enro.result

import android.widget.TextView
import dev.enro.TestActivity
import dev.enro.TestFragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.navigationHandle
import dev.enro.core.result.closeWithResult
import dev.enro.core.result.forwardNavigationResult
import dev.enro.core.result.registerForNavigationResult
import kotlinx.android.parcel.Parcelize

@Parcelize
class ActivityResultKey : NavigationKey.WithResult<String>

@NavigationDestination(ActivityResultKey::class)
class ResultActivity : TestActivity()

@Parcelize
class FragmentResultKey : NavigationKey.WithResult<String>

@NavigationDestination(FragmentResultKey::class)
class ResultFragment : TestFragment()

@Parcelize
class NestedResultFragmentKey : NavigationKey.WithResult<String>

@NavigationDestination(NestedResultFragmentKey::class)
class NestedResultFragment : TestFragment()


@Parcelize
class ResultReceiverActivityKey : NavigationKey

@NavigationDestination(ResultReceiverActivityKey::class)
class ResultReceiverActivity : TestActivity() {

    private val navigation by navigationHandle<ResultReceiverActivityKey> {
        defaultKey(ResultReceiverActivityKey())

        container(primaryFragmentContainer) { it is NestedResultFragmentKey }
    }

    var result: String? = null
    val resultChannel by registerForNavigationResult<String> {
        result = it
        findViewById<TextView>(debugText).text = "Result: $result\nSecondary Result: $secondaryResult\nFlow Result: $flowResult"
    }

    var secondaryResult: String? = null
    val secondaryResultChannel by registerForNavigationResult<String> {
        secondaryResult = it
        findViewById<TextView>(debugText).text = "Result: $result\nSecondary Result: $secondaryResult\nFlow Result: $flowResult"
    }

    var flowResult: FlowTestResult? = null
    val flowResultChannel by registerForNavigationResult<FlowTestResult> {
        flowResult = it
        findViewById<TextView>(debugText).text = "Result: $result\nSecondary Result: $secondaryResult\nFlow Result: $flowResult"
    }
}


@Parcelize
class NestedResultReceiverActivityKey : NavigationKey

@NavigationDestination(NestedResultReceiverActivityKey::class)
class NestedResultReceiverActivity : TestActivity() {
    private val navigation by navigationHandle<NestedResultReceiverActivityKey> {
        defaultKey(NestedResultReceiverActivityKey())
        container(primaryFragmentContainer) { it is ResultReceiverFragmentKey || it is NestedResultFragmentKey }
    }
}

@Parcelize
class SideBySideNestedResultReceiverActivityKey : NavigationKey

@NavigationDestination(SideBySideNestedResultReceiverActivityKey::class)
class SideBySideNestedResultReceiverActivity : TestActivity() {
    private val navigation by navigationHandle<SideBySideNestedResultReceiverActivityKey> {
        defaultKey(SideBySideNestedResultReceiverActivityKey())
        container(primaryFragmentContainer) { it is ResultReceiverFragmentKey }
        container(secondaryFragmentContainer) { it is NestedResultFragmentKey }
    }
}

@Parcelize
class ResultReceiverFragmentKey : NavigationKey

@NavigationDestination(ResultReceiverFragmentKey::class)
class ResultReceiverFragment : TestFragment() {
    var result: String? = null
    val resultChannel by registerForNavigationResult<String> {
        result = it
        requireView().findViewById<TextView>(debugText).text = "Result: $result\nSecondary Result: $secondaryResult"
    }

    var secondaryResult: String? = null
    val secondaryResultChannel by registerForNavigationResult<String> {
        secondaryResult = it
        requireView().findViewById<TextView>(debugText).text = "Result: $result\nSecondary Result: $secondaryResult"
    }
}

@Parcelize
class NestedResultReceiverFragmentKey : NavigationKey

@NavigationDestination(NestedResultReceiverFragmentKey::class)
class NestedResultReceiverFragment : TestFragment() {

    private val navigation by navigationHandle<NestedResultReceiverFragmentKey> {
        container(primaryFragmentContainer) { it is NestedResultFragmentKey }
    }

    var result: String? = null
    val resultChannel by registerForNavigationResult<String> {
        result = it
        requireView().findViewById<TextView>(debugText).text = "Result: $result\nSecondary Result: $secondaryResult"
    }

    var secondaryResult: String? = null
    val secondaryResultChannel by registerForNavigationResult<String> {
        secondaryResult = it
        requireView().findViewById<TextView>(debugText).text = "Result: $result\nSecondary Result: $secondaryResult"
    }
}


@Parcelize
class FlowTestPartOne() : NavigationKey.WithResult<FlowTestResult>

@Parcelize
data class FlowTestPartTwo(
        val firstData: String
) : NavigationKey.WithResult<FlowTestResult>

@Parcelize
data class FlowTestPartThree(
        val firstData: String,
        val secondData: String
) : NavigationKey.WithResult<FlowTestResult>

data class FlowTestResult(
        val firstData: String,
        val secondData: String,
        val thirdData: String
)

@NavigationDestination(FlowTestPartOne::class)
class FlowTestPartOneFragment : TestFragment() {
    private val navigation by navigationHandle<FlowTestPartOne>()
    val resultChannel by forwardNavigationResult<FlowTestResult>()

    fun next(data: String) {
        resultChannel.open(
                FlowTestPartTwo(
                        firstData = data
                )
        )
    }
}

@NavigationDestination(FlowTestPartTwo::class)
class FlowTestPartTwoFragment : TestFragment() {
    private val navigation by navigationHandle<FlowTestPartTwo>()
    val resultChannel by forwardNavigationResult<FlowTestResult>()

    fun next(data: String) {
        resultChannel.open(
                FlowTestPartThree(
                        firstData = navigation.key.firstData,
                        secondData = data
                )
        )
    }
}

@NavigationDestination(FlowTestPartThree::class)
class FlowTestPartThreeFragment : TestFragment() {
    private val navigation by navigationHandle<FlowTestPartThree>()

    fun finishFlow(data: String) {
        navigation.closeWithResult(
                FlowTestResult(
                        firstData = navigation.key.firstData,
                        secondData = navigation.key.secondData,
                        thirdData = data
                )
        )
    }
}