@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro.test

import dev.enro.EnroController

public object EnroTest {

    private var navigationController: EnroController? = null
    private var wasInstalled = false

    // TODO: support installing a NavigationModule for the duration of one test.
    public fun installNavigationController() {
        if (navigationController != null) {
            uninstallNavigationController()
        }

        // An installed controller is reused. In an Android instrumented test, the test
        // Application installs Enro before the test rule runs.
        navigationController = EnroController.instance
        if (navigationController != null) {
            wasInstalled = true
            return
        }

        // A test without an Application (commonTest on desktop, iOS or wasm, or a JVM unit
        // test) installs a new controller with a null platform reference. Only Android
        // runtime code (ActivityPlugin, EnroLog.android) reads
        // EnroController.platformReference, and that code handles null.
        navigationController = EnroController().apply {
            install(reference = null)
        }
        wasInstalled = false
    }

    public fun uninstallNavigationController() {
        // EnroTest uninstalls only a controller that EnroTest created.
        if (!wasInstalled) {
            navigationController?.uninstall()
        }
        navigationController = null

        @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
        dev.enro.viewmodel.NavigationHandleProvider.clearAllForTest()
    }

    public fun getCurrentNavigationController(): EnroController {
        return navigationController ?: throw IllegalStateException("NavigationController is not installed")
    }

    @Deprecated(
        message = "This function is a no-op in the current API and will be removed in a future release.",
        level = DeprecationLevel.WARNING,
    )
    public fun disableAnimations(controller: EnroController) {
    }

    @Deprecated(
        message = "This function is a no-op in the current API and will be removed in a future release.",
        level = DeprecationLevel.WARNING,
    )
    public fun enableAnimations(controller: EnroController) {
    }
}
