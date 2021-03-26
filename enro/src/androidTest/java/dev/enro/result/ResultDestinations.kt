package dev.enro.result

import android.widget.TextView
import kotlinx.parcelize.Parcelize
import dev.enro.TestActivity
import dev.enro.TestFragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.navigationHandle
import dev.enro.core.result.registerForNavigationResult

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
        findViewById<TextView>(debugText).text = "Result: $result\nSecondary Result: $secondaryResult"
    }

    var secondaryResult: String? = null
    val secondaryResultChannel by registerForNavigationResult<String> {
        secondaryResult = it
        findViewById<TextView>(debugText).text = "Result: $result\nSecondary Result: $secondaryResult"
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