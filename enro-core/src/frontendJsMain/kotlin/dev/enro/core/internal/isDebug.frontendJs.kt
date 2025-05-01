package dev.enro.core.internal

import kotlinx.browser.document

public actual inline fun isDebugBuild(): Boolean {
    return document.location?.host?.startsWith("localhost") == true
}