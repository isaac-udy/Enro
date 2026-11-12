package dev.enro.recipes.synthetic

import dev.enro.NavigationContext
import java.awt.Desktop
import java.net.URI

internal actual fun launchExternalUrl(context: NavigationContext, url: String) {
    Desktop.getDesktop().browse(URI(url))
}
