package dev.enro.core.controller

import platform.UIKit.UIApplication

public val UIApplication.enroNavigationController: NavigationController
    get() {
        return requireNotNull(NavigationController.navigationController) {
            // TODO better error message? Use NavigationApplication for iOS too?
            "NavigationController has not been installed"
        }
    }