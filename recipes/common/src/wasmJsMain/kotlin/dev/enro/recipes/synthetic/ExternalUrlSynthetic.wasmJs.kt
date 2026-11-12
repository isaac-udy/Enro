package dev.enro.recipes.synthetic

import dev.enro.NavigationContext
import kotlinx.browser.window

internal actual fun launchExternalUrl(context: NavigationContext, url: String) {
    window.open(url, "_blank")
}
