package dev.enro.core.compose.destination

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.savedstate.SavedStateRegistryOwner

internal actual val EnroLocalSavedStateRegistryOwner: ProvidableCompositionLocal<SavedStateRegistryOwner>
    get() = LocalSavedStateRegistryOwner