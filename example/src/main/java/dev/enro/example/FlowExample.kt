package dev.enro.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.navigationHandle
import dev.enro.core.result.closeWithResult
import dev.enro.core.result.forwardNavigationResult
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_request_string.*

@Parcelize
class FlowPartOne() : NavigationKey.WithResult<FlowResult>

@Parcelize
class FlowPartOneA() : NavigationKey.WithResult<FlowResult>

@Parcelize
data class FlowPartTwo(
        val firstData: String
) : NavigationKey.WithResult<FlowResult>

@Parcelize
data class FlowPartTwoA(
    val firstData: String
) : NavigationKey.WithResult<FlowResult>

@Parcelize
data class FlowPartThree(
        val firstData: String,
        val secondData: String
) : NavigationKey.WithResult<FlowResult>

@Parcelize
data class FlowPartThreeA(
    val firstData: String,
    val secondData: String
) : NavigationKey.WithResult<FlowResult>

data class FlowResult(
        val firstData: String,
        val secondData: String,
        val thirdData: String
)

@NavigationDestination(FlowPartOne::class)
class FlowPartOneFragment : Fragment() {

    private val navigation by navigationHandle<FlowPartOne>()
    private val forwardChannel by forwardNavigationResult<FlowResult>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_request_string, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        title.text = "Flow Part One"
        sendResultButton.text = "Continue Fragment"
        sendResultButton.setOnClickListener {
            forwardChannel.open(FlowPartTwo(
                firstData = input.text.toString()
            ))
        }
        secondaryButton.isVisible = true
        secondaryButton.text = "Continue Activity"
        secondaryButton.setOnClickListener {
            forwardChannel.open(FlowPartTwoA(
                firstData = input.text.toString()
            ))
        }
    }
}

@NavigationDestination(FlowPartOneA::class)
class FlowPartOneActivity : FragmentActivity() {
    private val navigation by navigationHandle<FlowPartOneA>()
    private val forwardChannel by forwardNavigationResult<FlowResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_request_string)
        findViewById<TextView>(R.id.title).text = "Flow Part One Activity"
        sendResultButton.text = "Continue Fragment"
        sendResultButton.setOnClickListener {
            forwardChannel.open(FlowPartTwo(
                firstData = input.text.toString()
            ))
        }
        secondaryButton.isVisible = true
        secondaryButton.text = "Continue Activity"
        secondaryButton.setOnClickListener {
            forwardChannel.open(FlowPartTwoA(
                firstData = input.text.toString()
            ))
        }
    }
}

@NavigationDestination(FlowPartTwo::class)
class FlowPartTwoFragment : Fragment() {

    private val navigation by navigationHandle<FlowPartTwo>()
    private val forwardChannel by forwardNavigationResult<FlowResult>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_request_string, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        title.text = "Flow Part Two"
        sendResultButton.text = "Continue Fragment"
        sendResultButton.setOnClickListener {
            forwardChannel.open(FlowPartThree(
                firstData = navigation.key.firstData,
                secondData = input.text.toString()
            ))
        }
        secondaryButton.isVisible = true
        secondaryButton.text = "Continue Activity"
        secondaryButton.setOnClickListener {
            forwardChannel.open(FlowPartThreeA(
                firstData = navigation.key.firstData,
                secondData = input.text.toString()
            ))
        }
    }
}

@NavigationDestination(FlowPartTwoA::class)
class FlowPartTwoActivity : FragmentActivity() {
    private val navigation by navigationHandle<FlowPartTwoA>()
    private val forwardChannel by forwardNavigationResult<FlowResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_request_string)
        findViewById<TextView>(R.id.title).text = "Flow Part Two Activity"
        sendResultButton.text = "Continue Fragment"
        sendResultButton.setOnClickListener {
            forwardChannel.open(FlowPartThree(
                firstData = navigation.key.firstData,
                secondData = input.text.toString()
            ))
        }
        secondaryButton.isVisible = true
        secondaryButton.text = "Continue Activity"
        secondaryButton.setOnClickListener {
            forwardChannel.open(FlowPartThreeA(
                firstData = navigation.key.firstData,
                secondData = input.text.toString()
            ))
        }
    }
}

@NavigationDestination(FlowPartThree::class)
class FlowPartThreeFragment : Fragment() {

    private val navigation by navigationHandle<FlowPartThree>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_request_string, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        title.text = "Flow Part Three"
        sendResultButton.text = "Finish"
        sendResultButton.setOnClickListener {
            navigation.closeWithResult(FlowResult(
                    firstData = navigation.key.firstData,
                    secondData = navigation.key.secondData,
                    thirdData = input.text.toString()
            ))
        }
    }
}

@NavigationDestination(FlowPartThreeA::class)
class FlowPartThreeActivity : FragmentActivity() {
    private val navigation by navigationHandle<FlowPartThreeA>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_request_string)
        findViewById<TextView>(R.id.title).text = "Flow Part Three Activity"
        sendResultButton.text = "Finish"
        sendResultButton.setOnClickListener {
            navigation.closeWithResult(FlowResult(
                firstData = navigation.key.firstData,
                secondData = navigation.key.secondData,
                thirdData = input.text.toString()
            ))
        }
    }
}
