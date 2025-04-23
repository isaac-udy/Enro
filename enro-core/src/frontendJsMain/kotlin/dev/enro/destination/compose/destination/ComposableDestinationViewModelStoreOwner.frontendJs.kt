package dev.enro.core.compose.destination

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.savedstate.SavedState

internal actual fun createViewModelFactory(
    owner: ComposableDestinationOwner,
    savedState: SavedState
): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {

    }
}

internal actual fun MutableCreationExtras.addPlatformExtras(owner: ComposableDestinationOwner) {

}