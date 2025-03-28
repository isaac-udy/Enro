package dev.enro.tests.application.compose

import android.os.Parcelable
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.navigationHandle
import dev.enro.core.present
import dev.enro.core.synthetic.syntheticDestination
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.viewmodel.requireViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.Parcelize

@Parcelize
object SyntheticViewModelAccess : Parcelable, NavigationKey.SupportsPush {
    @Parcelize
    internal object AccessValidViewModel : Parcelable, NavigationKey.SupportsPresent

    @Parcelize
    internal object AccessInvalidViewModel : Parcelable, NavigationKey.SupportsPresent
}

class ViewModelForSyntheticViewModelAccess : ViewModel() {
    private val mutableState = MutableStateFlow(0)
    val state = mutableState as StateFlow<Int>

    fun onViewModelAccessed() {
        mutableState.update { it+1 }
    }
}

@NavigationDestination(SyntheticViewModelAccess::class)
@Composable
fun SyntheticViewModelAccessDestination() {
    val navigationHandle = navigationHandle()
    val viewModel = viewModel<ViewModelForSyntheticViewModelAccess>()
    val state by viewModel.state.collectAsState()
    TitledColumn(
        "Synthetic ViewModel Access"
    ) {
        Text(text = "ViewModel Accessed $state times")

        Button(onClick = {
            navigationHandle.present(SyntheticViewModelAccess.AccessValidViewModel)
        }) {
            Text(text = "Access Valid ViewModel")
        }

        Button(onClick = {
            navigationHandle.present(SyntheticViewModelAccess.AccessInvalidViewModel)
        }) {
            Text(text = "Access Invalid ViewModel (throws)")
        }
    }
}

// This destination should successfully access the ViewModelForSyntheticViewModelAccess from the Destination defined above,
// and trigger a side effect on that ViewModel
@NavigationDestination(SyntheticViewModelAccess.AccessValidViewModel::class)
internal val accessValidViewModel = syntheticDestination<SyntheticViewModelAccess.AccessValidViewModel> {
    require(navigationContext.instruction?.navigationKey is SyntheticViewModelAccess)

    navigationContext.requireViewModel<ViewModelForSyntheticViewModelAccess>()
        .onViewModelAccessed()
}

class InvalidViewModel : ViewModel()

// This destination should throw an exception for attempting to access an invalid ViewModel
@NavigationDestination(SyntheticViewModelAccess.AccessInvalidViewModel::class)
internal val accessInvalidViewModel = syntheticDestination<SyntheticViewModelAccess.AccessInvalidViewModel> {
    require(navigationContext.instruction?.navigationKey is SyntheticViewModelAccess)

    navigationContext.requireViewModel<InvalidViewModel>()
}
