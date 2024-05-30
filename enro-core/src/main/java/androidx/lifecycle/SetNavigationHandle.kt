package androidx.lifecycle

import dev.enro.core.NavigationHandle
import java.io.Closeable

internal const val NAVIGATION_HANDLE_KEY = "dev.enro.viemodel.NAVIGATION_HANDLE_KEY"

internal class ClosableNavigationHandleReference(
    navigationHandle: NavigationHandle,
) : Closeable {
    var navigationHandle: NavigationHandle? = navigationHandle
    override fun close() {
        navigationHandle = null
    }
}

internal fun ViewModel.setNavigationHandleTag(navigationHandle: NavigationHandle) {
    addCloseable(
        NAVIGATION_HANDLE_KEY,
        ClosableNavigationHandleReference(navigationHandle),
    )

}

internal fun ViewModel.getNavigationHandleTag(): NavigationHandle? {
    val closeable = getCloseable(
        NAVIGATION_HANDLE_KEY
    ) as? ClosableNavigationHandleReference
    return closeable?.navigationHandle
}