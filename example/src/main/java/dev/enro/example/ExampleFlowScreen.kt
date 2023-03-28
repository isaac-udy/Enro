package dev.enro.example

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.result.registerForFlowResult
import dev.enro.viewmodel.enroViewModels
import dev.enro.viewmodel.navigationHandle
import kotlinx.parcelize.Parcelize

@Parcelize
class ExampleFlowKey : NavigationKey.SupportsPresent, NavigationKey.SupportsPush

data class FlowSteps(
    val first: String,
    val second: String,
    val bottomSheet: String,
    val third: String,
)

class FlowViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val navigation by navigationHandle<NavigationKey>()
    val flow by registerForFlowResult(
        flow = {
            val first = push(RequestStringKey())
            val second = push(RequestStringKey())
            val bottomSheet = present(RequestStringBottomSheetKey(), listOf(second))
            val third = push(RequestStringKey())
            FlowSteps(
                first = first,
                second = second,
                bottomSheet = bottomSheet,
                third = third,
            )
        },
        onCompleted = {
            Log.e("Finished", "$it")
            navigation.close()
        }
    )
}

@NavigationDestination(ExampleFlowKey::class)
class ExampleFlowFragment : Fragment() {
    private val navigationContainer by navigationContainer(R.id.flowContainer, emptyBehavior = EmptyBehavior.CloseParent)
    private val viewModel by enroViewModels<FlowViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_flow, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(savedInstanceState == null) viewModel.flow.next()
    }
}