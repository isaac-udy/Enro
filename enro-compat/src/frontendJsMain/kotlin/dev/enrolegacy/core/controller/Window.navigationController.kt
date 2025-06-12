package dev.enrolegacy.core.controller

import org.w3c.dom.Window

public val Window.navigationController: NavigationController
    get() = NavigationController.navigationController
        ?: error("No navigation controller found for this window. Did you forget to install it?")