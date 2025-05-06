package dev.enro.core.controller

import platform.UIKit.UIApplication

public val UIApplication.enroNavigationController: NavigationController
    get() {
        return requireNotNull(NavigationController.navigationController) {
            "NavigationController has not been installed"
        }
    }