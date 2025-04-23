package dev.enro.core.controller.usecase

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import dev.enro.core.NavigationHandle
import dev.enro.core.controller.get

/**
 * NavigationHandleExtras provides a centralised place to store additional objects on a NavigationHandle.
 *
 * For example, plugins that extend Enro's functionality may want to use the NavigationHandle.extras property to
 * attach their own objects to the NavigationHandle. Note that the extras are *not* recreated when a NavigationHandle
 * is recreated, they must be bound every time a NavigationHandle object is created. For example, in the `onOpened`
 * method of an EnroPlugin.
 *
 * Extras which extend java.io.Closeable will automatically have their close method called when the NavigationHandle is destroyed.
 */
public class NavigationHandleExtras : AutoCloseable {
    internal val extras: SnapshotStateMap<String, Any> = mutableStateMapOf()

    override fun close() {
        extras.values.forEach {
            if(it is AutoCloseable) it.close()
        }
        extras.clear()
    }
}

/**
 * Access the extras map on a NavigationHandle.
 *
 * NavigationHandle.extras can be used for storing additional information or state with a NavigationHandle. Extras are not
 * recreated when a NavigationHandle is recreated, and must be bound every time a NavigationHandle object is created.
 *
 * Extras which extend java.io.Closeable will automatically have their close method called when the NavigationHandle is destroyed.
 *
 * @see [NavigationHandleExtras]
 */
public val NavigationHandle.extras: MutableMap<String, Any>
    get() = dependencyScope.get<NavigationHandleExtras>().extras