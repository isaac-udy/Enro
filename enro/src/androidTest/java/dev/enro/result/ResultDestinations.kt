package dev.enro.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModel
import kotlinx.parcelize.Parcelize
import dev.enro.TestActivity
import dev.enro.TestFragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.result.closeWithResult
import dev.enro.core.result.forwardResult
import dev.enro.core.result.registerForNavigationResult
import dev.enro.core.result.sendResult
import dev.enro.core.synthetic.SyntheticDestination
import dev.enro.viewmodel.enroViewModels
import dev.enro.viewmodel.navigationHandle

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

    }
    private val primaryContainer by navigationContainer(primaryFragmentContainer) { it is NestedResultFragmentKey }

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
    }
    private val primaryContainer by navigationContainer(primaryFragmentContainer) { it is ResultReceiverFragmentKey || it is NestedResultFragmentKey }
}

@Parcelize
class SideBySideNestedResultReceiverActivityKey : NavigationKey

@NavigationDestination(SideBySideNestedResultReceiverActivityKey::class)
class SideBySideNestedResultReceiverActivity : TestActivity() {
    private val navigation by navigationHandle<SideBySideNestedResultReceiverActivityKey> {
        defaultKey(SideBySideNestedResultReceiverActivityKey())
    }
    private val primaryContainer by navigationContainer(primaryFragmentContainer) { it is ResultReceiverFragmentKey }
    private val secondaryContainer by navigationContainer(secondaryFragmentContainer) { it is NestedResultFragmentKey }
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

    private val navigation by navigationHandle<NestedResultReceiverFragmentKey>()

    private val primaryContainer by navigationContainer(primaryFragmentContainer) { it is NestedResultFragmentKey }

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
class ImmediateSyntheticResultKey(
    val reversedResult: String
) : NavigationKey.WithResult<String>

@NavigationDestination(ImmediateSyntheticResultKey::class)
class ImmediateSyntheticResultDestination : SyntheticDestination<ImmediateSyntheticResultKey>() {
    override fun process() {
        sendResult(key.reversedResult.reversed())
    }
}

@Parcelize
class ForwardingSyntheticActivityResultKey : NavigationKey.WithResult<String>

@NavigationDestination(ForwardingSyntheticActivityResultKey::class)
class ForwardingSyntheticActivityResultDestination : SyntheticDestination<ForwardingSyntheticActivityResultKey>() {
    override fun process() {
        forwardResult(ActivityResultKey())
    }
}

@Parcelize
class ForwardingSyntheticFragmentResultKey : NavigationKey.WithResult<String>

@NavigationDestination(ForwardingSyntheticFragmentResultKey::class)
class ForwardingSyntheticFragmentResultDestination : SyntheticDestination<ForwardingSyntheticFragmentResultKey>() {
    override fun process() {
        forwardResult(FragmentResultKey())
    }
}

class ViewModelForwardingResultViewModel : ViewModel() {
    val navigation by navigationHandle<NavigationKey.WithResult<String>>()
    val forwardingChannel by registerForNavigationResult<String>(navigation) {
        navigation.closeWithResult(it)
    }

    init {
        forwardingChannel.open(ActivityResultKey())
    }

}

@Parcelize
class ViewModelForwardingResultActivityKey : NavigationKey.WithResult<String>

@NavigationDestination(ViewModelForwardingResultActivityKey::class)
class ViewModelForwardingResultActivity : TestActivity() {
    private val viewModel by enroViewModels<ViewModelForwardingResultViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.hashCode()
    }
}

@Parcelize
class ViewModelForwardingResultFragmentKey : NavigationKey.WithResult<String>

@NavigationDestination(ViewModelForwardingResultFragmentKey::class)
class ViewModelForwardingResultFragment : TestFragment() {
    private val viewModel by enroViewModels<ViewModelForwardingResultViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.hashCode()
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

@Parcelize
class ResultFlowKey : NavigationKey

@NavigationDestination(ResultFlowKey::class)
class ResultFlowActivity : TestActivity() {
    private val viewModel by enroViewModels<ResultFlowViewModel>()
    private val navigation by navigationHandle<ResultFlowKey>()

    private val primaryContainer by navigationContainer(primaryFragmentContainer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.hashCode()
    }
}

class ResultFlowViewModel : ViewModel() {
    val navigation by navigationHandle<ResultFlowKey>()
    val first by registerForNavigationResult<String>(navigation) {
        if(it == "close") {
            navigation.close()
        }
        else {
            second.open(FragmentResultKey())
        }
    }

    val second by registerForNavigationResult<String>(navigation) {
        if(it == "close") {
            navigation.close()
        }
        else {
            third.open(FragmentResultKey())
        }
    }

    val third by registerForNavigationResult<String>(navigation) {
        navigation.close()
    }

    init {
        first.open(FragmentResultKey())
    }
}