package dev.enro.result

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModel
import dev.enro.TestActivity
import dev.enro.TestDialogFragment
import dev.enro.TestFragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.closeWithResult
import dev.enro.core.container.acceptKey
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.navigationHandle
import dev.enro.core.result.forwardResult
import dev.enro.core.result.registerForNavigationResult
import dev.enro.core.result.sendResult
import dev.enro.destination.synthetic.SyntheticDestination
import dev.enro.viewmodel.enroViewModels
import dev.enro.viewmodel.navigationHandle
import kotlinx.parcelize.Parcelize

@Parcelize
class ActivityResultKey : Parcelable, NavigationKey.SupportsPresent.WithResult<String>

@NavigationDestination(ActivityResultKey::class)
class ResultActivity : TestActivity()

@Parcelize
class FragmentResultKey : Parcelable, NavigationKey.SupportsPush.WithResult<String>

@NavigationDestination(FragmentResultKey::class)
class ResultFragment : TestFragment()

@Parcelize
class NestedResultFragmentKey : Parcelable, NavigationKey.SupportsPush.WithResult<String>

@NavigationDestination(NestedResultFragmentKey::class)
class NestedResultFragment : TestFragment()


@Parcelize
class ResultReceiverActivityKey : Parcelable, NavigationKey

@NavigationDestination(ResultReceiverActivityKey::class)
class ResultReceiverActivity : TestActivity() {

    private val navigation by navigationHandle<ResultReceiverActivityKey> {
        defaultKey(ResultReceiverActivityKey())

    }
    private val primaryContainer by navigationContainer(primaryFragmentContainer, filter = acceptKey {
        it is NestedResultFragmentKey
    })

    var result: String? = null
    var closedNoResult: Boolean = false
    val resultChannel by registerForNavigationResult<String>(
        onClosed = {
            closedNoResult = true
        }
    ) {
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
class NestedResultReceiverActivityKey : Parcelable, NavigationKey

@NavigationDestination(NestedResultReceiverActivityKey::class)
class NestedResultReceiverActivity : TestActivity() {
    private val navigation by navigationHandle<NestedResultReceiverActivityKey> {
        defaultKey(NestedResultReceiverActivityKey())
    }
    private val primaryContainer by navigationContainer(primaryFragmentContainer, filter = acceptKey { it is ResultReceiverFragmentKey || it is NestedResultFragmentKey })
}

@Parcelize
class SideBySideNestedResultReceiverActivityKey : Parcelable, NavigationKey

@NavigationDestination(SideBySideNestedResultReceiverActivityKey::class)
class SideBySideNestedResultReceiverActivity : TestActivity() {
    private val navigation by navigationHandle<SideBySideNestedResultReceiverActivityKey> {
        defaultKey(SideBySideNestedResultReceiverActivityKey())
    }
    private val primaryContainer by navigationContainer(primaryFragmentContainer, filter = acceptKey { it is ResultReceiverFragmentKey })
    private val secondaryContainer by navigationContainer(secondaryFragmentContainer, filter = acceptKey { it is NestedResultFragmentKey })
}

@Parcelize
class ResultReceiverFragmentKey : Parcelable, NavigationKey.SupportsPush

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
class NestedResultReceiverFragmentKey : Parcelable, NavigationKey.SupportsPush

@NavigationDestination(NestedResultReceiverFragmentKey::class)
class NestedResultReceiverFragment : TestFragment() {

    private val navigation by navigationHandle<NestedResultReceiverFragmentKey>()

    private val primaryContainer by navigationContainer(primaryFragmentContainer, filter = acceptKey { it is NestedResultFragmentKey })

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
) : Parcelable, NavigationKey.SupportsPresent.WithResult<String>

@NavigationDestination(ImmediateSyntheticResultKey::class)
class ImmediateSyntheticResultDestination : SyntheticDestination<ImmediateSyntheticResultKey>() {
    override fun process() {
        sendResult(key.reversedResult.reversed())
    }
}

@Parcelize
class ForwardingSyntheticActivityResultKey : Parcelable, NavigationKey.SupportsPresent.WithResult<String>

@NavigationDestination(ForwardingSyntheticActivityResultKey::class)
class ForwardingSyntheticActivityResultDestination : SyntheticDestination<ForwardingSyntheticActivityResultKey>() {
    override fun process() {
        forwardResult(ActivityResultKey())
    }
}

@Parcelize
class ForwardingSyntheticFragmentResultKey : Parcelable, NavigationKey.SupportsPresent.WithResult<String>

@NavigationDestination(ForwardingSyntheticFragmentResultKey::class)
class ForwardingSyntheticFragmentResultDestination : SyntheticDestination<ForwardingSyntheticFragmentResultKey>() {
    override fun process() {
        forwardResult(FragmentResultKey())
    }
}

class ViewModelForwardingResultViewModel : ViewModel() {
    val navigation by navigationHandle<NavigationKey.WithResult<String>>()
    val forwardingChannel by registerForNavigationResult<String> {
        Log.e("Enro", "ViewModelForwardingResultViewModel received result ${navigation.lifecycle.currentState}")
        navigation.closeWithResult(it)
    }

    init {
        forwardingChannel.present(ActivityResultKey())
    }

}

@Parcelize
class ViewModelForwardingResultActivityKey : Parcelable, NavigationKey.SupportsPresent.WithResult<String>

@NavigationDestination(ViewModelForwardingResultActivityKey::class)
class ViewModelForwardingResultActivity : TestActivity() {
    private val viewModel by enroViewModels<ViewModelForwardingResultViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.hashCode()
    }
}

@Parcelize
class ViewModelForwardingResultFragmentKey : Parcelable, NavigationKey.SupportsPush.WithResult<String>

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
class ResultFlowKey : Parcelable, NavigationKey.SupportsPush

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
    val first by registerForNavigationResult<String> {
        if(it == "close") {
            navigation.close()
        }
        else {
            second.push(FragmentResultKey())
        }
    }

    val second by registerForNavigationResult<String> {
        if(it == "close") {
            navigation.close()
        }
        else {
            third.push(FragmentResultKey())
        }
    }

    val third by registerForNavigationResult<String> {
        navigation.close()
    }

    init {
        first.push(FragmentResultKey())
    }
}


@Parcelize
class ResultFlowDialogFragmentRootKey : Parcelable, NavigationKey.SupportsPresent.WithResult<String>

@NavigationDestination(ResultFlowDialogFragmentRootKey::class)
class ResultFlowFragmentRootActivity : TestActivity() {
    private val navigation by navigationHandle<ResultFlowDialogFragmentRootKey> {
        defaultKey(ResultFlowDialogFragmentRootKey())
    }

    private val primaryContainer by navigationContainer(primaryFragmentContainer, filter = acceptKey { it is ResultFlowDialogFragmentKey })

    var lastResult: String = ""
    val nestedResult by registerForNavigationResult<String> {
        lastResult = it
    }

    override fun onResume() {
        super.onResume()
        nestedResult
            .present(ResultFlowDialogFragmentKey())
    }
}

@Parcelize
class ResultFlowDialogFragmentKey : Parcelable, NavigationKey.SupportsPresent.WithResult<String>

@NavigationDestination(ResultFlowDialogFragmentKey::class)
class ResultFlowDialogFragment : TestDialogFragment() {
    val navigation by navigationHandle<ResultFlowDialogFragmentKey>()
    private val primaryContainer by navigationContainer(primaryFragmentContainer, filter = acceptKey {
        it is NestedResultFlowFragmentKey
    })

    val nestedResult by registerForNavigationResult<Int> {
        navigation.closeWithResult("*".repeat(it))
    }

    override fun onResume() {
        super.onResume()
        nestedResult
            .push(NestedResultFlowFragmentKey())
    }
}

@Parcelize
class NestedResultFlowFragmentKey : Parcelable, NavigationKey.SupportsPush.WithResult<Int>

@NavigationDestination(NestedResultFlowFragmentKey::class)
class NestedResultFlowFragment : TestFragment() {
    val navigation by navigationHandle<NestedResultFlowFragmentKey>()
    private val primaryContainer by navigationContainer(primaryFragmentContainer, filter = acceptKey {
        it is NestedNestedResultFlowFragmentKey
    })

    val nestedResult by registerForNavigationResult<Int> {
        navigation.closeWithResult(it)
    }

    override fun onResume() {
        super.onResume()
        nestedResult
            .push(NestedNestedResultFlowFragmentKey())
    }
}

@Parcelize
class NestedNestedResultFlowFragmentKey : Parcelable, NavigationKey.SupportsPush.WithResult<Int>

@NavigationDestination(NestedNestedResultFlowFragmentKey::class)
class NestedNestedResultFlowFragment : TestFragment() {
    val navigation by navigationHandle<NestedNestedResultFlowFragmentKey>()

    override fun onResume() {
        super.onResume()
        navigation.closeWithResult(6)
    }
}