package dev.enro.example.destinations.result

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.result.registerForNavigationResult
import dev.enro.example.R
import dev.enro.example.core.data.Sentence
import dev.enro.example.databinding.FragmentResultExampleBinding
import dev.enro.example.destinations.result.compose.GetString
import dev.enro.example.destinations.result.flow.embedded.CreateSentenceEmbeddedFlow
import dev.enro.example.destinations.result.flow.managed.CreateSentenceManagedFlow
import dev.enro.viewmodel.enroViewModels
import dev.enro.viewmodel.navigationHandle
import kotlinx.parcelize.Parcelize

@Parcelize
class ResultExampleKey : NavigationKey.SupportsPush

@SuppressLint("SetTextI18n")
@NavigationDestination(ResultExampleKey::class)
class RequestExampleFragment : Fragment() {

    private val viewModel by enroViewModels<RequestExampleViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_result_example, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        FragmentResultExampleBinding.bind(view).apply {
            viewModel.results.observe(viewLifecycleOwner) {
                results.text = it.joinToString("\n")
                if (it.isEmpty()) {
                    results.text = "(None)"
                }
            }

            requestStringButton.setOnClickListener {
                viewModel.onRequestStringPushed()
            }
            requestStringBottomSheetButton.setOnClickListener {
                viewModel.onRequestStringPresented()
            }
            requestSentenceManagedFlowButton.setOnClickListener {
                viewModel.onRequestSentenceManagedFlow()
            }
            requestSentenceEmbeddedFlowButton.setOnClickListener {
                viewModel.onRequestSentenceEmbeddedFlow()
            }
        }
    }
}

class RequestExampleViewModel() : ViewModel() {

    private val navigation by navigationHandle<ResultExampleKey>()

    private val mutableResults = MutableLiveData<List<String>>().apply { emptyList<String>() }
    val results = mutableResults as LiveData<List<String>>

    private val requestString by registerForNavigationResult<String> {
        mutableResults.value = mutableResults.value.orEmpty() + it
    }

    private val requestSentence by registerForNavigationResult<Sentence> {
        mutableResults.value = mutableResults.value.orEmpty() + it.asCamelCaseString()
    }

    fun onRequestStringPushed() {
        requestString.push(
            GetString("Get String from Push")
        )
    }

    fun onRequestStringPresented() {
        requestString.present(
            GetString("Get String from Present")
        )
    }

    fun onRequestSentenceManagedFlow() {
        requestSentence.push(CreateSentenceManagedFlow())
    }

    fun onRequestSentenceEmbeddedFlow() {
        requestSentence.push(CreateSentenceEmbeddedFlow())
    }
}