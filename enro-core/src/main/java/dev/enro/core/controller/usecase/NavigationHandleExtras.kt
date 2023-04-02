package dev.enro.core.controller.usecase

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap

public class NavigationHandleExtras {
    internal val extras: SnapshotStateMap<String, Any> = mutableStateMapOf()
}