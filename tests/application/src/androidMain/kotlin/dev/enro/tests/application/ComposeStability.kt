package dev.enro.tests.application

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro3.NavigationKey
import dev.enro3.navigationHandle
import dev.enro3.open
import dev.enro3.ui.navigationDestination
import dev.enro3.viewmodel.createEnroViewModel
import kotlinx.coroutines.isActive
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
class ComposeStabilityKey(
    val id: String = Uuid.random().toString()
) : NavigationKey

val composeStabilityDestination = navigationDestination<ComposeStabilityKey> {
    val navigation = navigationHandle<ComposeStabilityKey>()
    val viewModel = viewModel<ComposeStabilityViewModel> {
        createEnroViewModel {
            ComposeStabilityViewModel(SavedStateHandle())
        }
    }
    val savedStateId by viewModel.saveStateHandleId.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .padding(8.dp)
            .background(Color.Black.copy(alpha = 0.05f))
            .padding(8.dp)
    ) {
        Stability(
            additionalStabilityContent = listOf(
                "navigationKeyId: ${navigation.key.id}",
                "savedStateId: $savedStateId",
            ),
        )
        Button(onClick = {
            navigation.open(ComposeStabilityKey())
        }) {
            Text("Open Stability Again")
        }
    }
}

class ComposeStabilityViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val id: String = Uuid.random().toString()
    val saveStateHandleId = savedStateHandle.getStateFlow("savedStateId", Uuid.random().toString())
}

@Composable
fun Stability(
    modifier: Modifier = Modifier,
    additionalStabilityContent: List<String> = emptyList(),
) {
    val rawNavigationHandle = navigationHandle<NavigationKey>()
    val rememberSaveable = rememberSaveable { Uuid.random().toString() }
    val viewModel = viewModel<ComposeStabilityViewModel> {
        ComposeStabilityViewModel(createSavedStateHandle())
    }
    val viewModelStore = LocalViewModelStoreOwner.current?.viewModelStore

    val stabilityContent = buildString {
        appendLine("navigationId: ${rawNavigationHandle.id}")
        appendLine("navigationHashCode: ${rawNavigationHandle.hashCode()}")
        appendLine("viewModelId: ${viewModel.id}")
        appendLine("viewModelHashCode: ${viewModel.hashCode()}")
        appendLine("viewModelSavedStateId: ${viewModel.saveStateHandleId.value}")
        appendLine("viewModelStoreHashCode: ${viewModelStore.hashCode()}")
        appendLine("viewModelScopeActive: ${viewModel.viewModelScope.isActive}")
        appendLine("rememberSaveableId: $rememberSaveable")
        additionalStabilityContent.forEach {
            appendLine(it)
        }
    }
    Text(
        text = stabilityContent,
        fontSize = 11.sp,
        lineHeight = 13.sp,
        modifier = modifier,
    )
}