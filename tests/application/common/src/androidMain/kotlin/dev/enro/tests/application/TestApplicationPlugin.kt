package dev.enro.tests.application

import dev.enro.core.NavigationHandle
import dev.enro.core.plugins.EnroPlugin
import java.lang.ref.WeakReference

object TestApplicationPlugin : EnroPlugin() {
    private var activeHandleRef: WeakReference<NavigationHandle> = WeakReference(null)
    val activeNavigationHandle: NavigationHandle? get() = activeHandleRef.get()

    override fun onActive(navigationHandle: NavigationHandle) {
        activeHandleRef = WeakReference(navigationHandle)
    }

    override fun onClosed(navigationHandle: NavigationHandle) {
        val current = activeHandleRef.get() ?: return
        if (current.instance.id == navigationHandle.instance.id) {
            activeHandleRef.clear()
        }
    }
}