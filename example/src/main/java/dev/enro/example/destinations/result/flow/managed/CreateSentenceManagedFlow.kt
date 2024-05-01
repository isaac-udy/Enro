package dev.enro.example.destinations.result.flow.managed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.closeWithResult
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.result.flows.registerForFlowResult
import dev.enro.example.R
import dev.enro.example.core.data.Sentence
import dev.enro.example.destinations.result.compose.SelectAdjective
import dev.enro.example.destinations.result.compose.SelectAdverb
import dev.enro.example.destinations.result.compose.SelectNoun
import dev.enro.viewmodel.enroViewModels
import dev.enro.viewmodel.navigationHandle
import kotlinx.parcelize.Parcelize

@Parcelize
class CreateSentenceManagedFlow : NavigationKey.SupportsPresent.WithResult<Sentence>, NavigationKey.SupportsPush.WithResult<Sentence>

@OptIn(ExperimentalEnroApi::class)
class ManagedFlowViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val navigation by navigationHandle<CreateSentenceManagedFlow>()

    val flow by registerForFlowResult(
        savedStateHandle = savedStateHandle,
        flow = {
            val adverb = push { SelectAdverb }
            val adjective = push { SelectAdjective }
            val noun = push { SelectNoun }
            Sentence(
                adverb = adverb,
                adjective = adjective,
                noun = noun,
            )
        },
        onCompleted = {
            navigation.closeWithResult(it)
        }
    )
}

@NavigationDestination(CreateSentenceManagedFlow::class)
class ExampleManagedFlowFragment : Fragment() {
    private val navigationContainer by navigationContainer(R.id.flowContainer, emptyBehavior = EmptyBehavior.CloseParent)
    private val viewModel by enroViewModels<ManagedFlowViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_flow, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.hashCode()
    }
}