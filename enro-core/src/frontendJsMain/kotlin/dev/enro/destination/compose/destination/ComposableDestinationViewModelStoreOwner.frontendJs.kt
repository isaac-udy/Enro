package dev.enro.core.compose.destination

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.savedstate.SavedState

internal actual fun createViewModelFactory(
    owner: ComposableDestinationOwner,
    savedState: SavedState
): ViewModelProvider.Factory {
    TODO("Not yet implemented")
}

internal actual fun MutableCreationExtras.addPlatformExtras(owner: ComposableDestinationOwner): CreationExtras {
    TODO("Not yet implemented")
}