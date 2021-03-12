package dev.enro.example

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_request_string.*
import kotlinx.android.synthetic.main.fragment_result_example.*
import nav.enro.annotations.NavigationDestination
import nav.enro.core.NavigationKey
import nav.enro.core.navigationHandle
import nav.enro.core.result.closeWithResult
import nav.enro.core.result.registerForNavigationResult
import nav.enro.viewmodel.enroViewModels
import nav.enro.viewmodel.navigationHandle

@Parcelize
class ResultExampleKey : NavigationKey

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
        viewModel.results.observe(viewLifecycleOwner, Observer {
            results.text = it.joinToString("\n")
            if(it.isEmpty()) {
                results.text = "(None)"
            }
        })

        requestStringButton.setOnClickListener {
            viewModel.onRequestString()
        }
    }
}

class RequestExampleViewModel() : ViewModel() {

    private val navigation by navigationHandle<ResultExampleKey>()

    private val mutableResults = MutableLiveData<List<String>>().apply { emptyList<String>() }
    val results = mutableResults as LiveData<List<String>>

    private val requestString by registerForNavigationResult<String>(navigation) {
        mutableResults.value = mutableResults.value.orEmpty() + it
    }

    fun onRequestString() {
        requestString.open(RequestStringKey())
    }
}

@Parcelize
class RequestStringKey : ResultNavigationKey<String>

@NavigationDestination(RequestStringKey::class)
class RequestStringFragment : Fragment() {

    private val navigation by navigationHandle<RequestStringKey>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_request_string, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sendResultButton.setOnClickListener {
            navigation.closeWithResult(input.text.toString())
        }
    }
}