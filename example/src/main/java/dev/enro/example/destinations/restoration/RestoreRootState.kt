package dev.enro.example.destinations.restoration

import android.os.Bundle
import android.os.Parcelable
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.requireRootContainer
import dev.enro.core.synthetic.syntheticDestination
import kotlinx.parcelize.Parcelize

@Parcelize
data class RestoreRootState(val state: Bundle) : Parcelable, NavigationKey.SupportsPresent

@NavigationDestination(RestoreRootState::class)
val restoreRootState = syntheticDestination<RestoreRootState> {
    navigationContext
        .requireRootContainer()
        .childContext
        ?.containerManager
        ?.activeContainer
        ?.restore(key.state)
}