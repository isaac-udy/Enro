package dev.enro.core.compose.destination

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.savedstate.SavedStateRegistryOwner

public actual val EnroLocalSavedStateRegistryOwner: ProvidableCompositionLocal<SavedStateRegistryOwner> = staticCompositionLocalOf {
    error("No SavedStateRegistryOwner provided. Ensure you are using Enro's Compose Navigation.")
}