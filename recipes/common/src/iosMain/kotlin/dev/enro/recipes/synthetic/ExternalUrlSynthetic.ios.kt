package dev.enro.recipes.synthetic

import dev.enro.NavigationContext
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

internal actual fun launchExternalUrl(context: NavigationContext, url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(
        url = nsUrl,
        options = emptyMap<Any?, Any>(),
        completionHandler = null,
    )
}
