@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro.test

import dev.enro.EnroController

object EnroTest {

    private var navigationController: EnroController? = null
    private var wasInstalled = false

    // TODO: Would be nice to add functionality to temporarily install a NavigationModule for a particular test
    fun installNavigationController() {
        if (navigationController != null) {
            uninstallNavigationController()
        }

        // Reuse an already-installed controller if one is present — this is the
        // path Android-instrumented tests take, where the test Application has
        // already installed Enro via ActivityPlugin before the test rule runs.
        navigationController = EnroController.instance
        if (navigationController != null) {
            wasInstalled = true
            return
        }

        // For commonTest running on desktop/iOS/wasm — and for unit tests that
        // need a fresh controller without going through an Application — we
        // install with a null platform reference. EnroController.platformReference
        // is only consumed by Android-specific runtime code (ActivityPlugin,
        // EnroLog.android), all of which null-check before use, so this is safe.
        navigationController = EnroController().apply {
            install(reference = null)
        }
        wasInstalled = false
    }

    fun uninstallNavigationController() {
        // Only uninstall if we created it
        if (!wasInstalled) {
            navigationController?.uninstall()
        }
        navigationController = null

        @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
        dev.enro.viewmodel.NavigationHandleProvider.clearAllForTest()
    }

    fun getCurrentNavigationController(): EnroController {
        return navigationController ?: throw IllegalStateException("NavigationController is not installed")
    }

    fun disableAnimations(controller: EnroController) {
        // Animation control might need to be handled differently in the new API
        // For now, we'll leave this as a no-op
    }

    fun enableAnimations(controller: EnroController) {
        // Animation control might need to be handled differently in the new API
        // For now, we'll leave this as a no-op
    }
}
