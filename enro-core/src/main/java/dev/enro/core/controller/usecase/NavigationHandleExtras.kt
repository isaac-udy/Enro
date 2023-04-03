package dev.enro.core.controller.usecase

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import dev.enro.core.NavigationHandle
import dev.enro.core.controller.get
import java.io.Closeable

public class NavigationHandleExtras : Closeable {
    internal val extras: SnapshotStateMap<String, Any> = mutableStateMapOf()

    override fun close() {
        extras.values.forEach {
            if(it is Closeable) it.close()
        }
        extras.clear()
    }
}

public val NavigationHandle.extras: MutableMap<String, Any>
    get() = dependencyScope.get<NavigationHandleExtras>().extras