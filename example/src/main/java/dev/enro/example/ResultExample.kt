package dev.enro.example

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import dev.enro.annotations.ExperimentalComposableDestination
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.navigationHandle
import dev.enro.core.navigationHandle
import dev.enro.core.result.closeWithResult
import dev.enro.core.result.registerForNavigationResult
import dev.enro.example.databinding.FragmentRequestStringBinding
import dev.enro.example.databinding.FragmentResultExampleBinding
import dev.enro.viewmodel.enroViewModels
import dev.enro.viewmodel.navigationHandle
import kotlinx.parcelize.Parcelize

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
        FragmentResultExampleBinding.bind(view).apply {
            viewModel.results.observe(viewLifecycleOwner, Observer {
                results.text = it.joinToString("\n")
                if (it.isEmpty()) {
                    results.text = "(None)"
                }
            })

            requestStringButton.setOnClickListener {
                viewModel.onRequestString()
            }
            requestStringBottomSheetButton.setOnClickListener {
                viewModel.onRequestStringFromBottomSheet()
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

    fun onRequestString() {
        requestString.open(RequestStringKey())
    }

    fun onRequestStringFromBottomSheet() {
        requestString.open(RequestStringBottomSheetKey())
    }
}

@Parcelize
class RequestStringKey : NavigationKey.WithResult<String>

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
        FragmentRequestStringBinding.bind(view).apply {
            sendResultButton.setOnClickListener {
                navigation.closeWithResult(input.text.toString())
            }
        }
    }
}

@Parcelize
class RequestStringBottomSheetKey : NavigationKey.WithResult<String>

@OptIn(ExperimentalMaterialApi::class)
@Composable
@NavigationDestination(RequestStringBottomSheetKey::class)
@ExperimentalComposableDestination
fun BottomSheetDestination.RequestStringBottomSheet() {
    val navigation = navigationHandle<RequestStringBottomSheetKey>()
    val result = remember {
        mutableStateOf("")
    }

    EnroExampleTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 32.dp,
                    bottom = 32.dp
                )
        ) {
            Text(text = "Request String Bottom Sheet")
            OutlinedTextField(value = result.value, onValueChange = {
                result.value = it
            })
            OutlinedButton(onClick = {
                navigation.closeWithResult(result.value)
            }) {
                Text(text = "Send Result")
            }
        }
    }
}