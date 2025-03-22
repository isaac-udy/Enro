package dev.enro.tests.application.compose.common

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.core.compose.navigationHandle
import dev.enro.tests.application.compose.ComposeStabilityContentViewModel
import kotlinx.coroutines.isActive
import kotlin.uuid.Uuid

@Composable
fun Stability(
    modifier: Modifier = Modifier,
    additionalStabilityContent: List<String> = emptyList(),
) {
    val rawNavigationHandle = navigationHandle()
    val rememberSaveable = rememberSaveable { Uuid.random().toString() }
    val viewModel = viewModel<ComposeStabilityContentViewModel>()
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