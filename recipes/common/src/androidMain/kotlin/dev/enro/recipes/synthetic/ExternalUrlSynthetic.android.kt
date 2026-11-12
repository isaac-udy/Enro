package dev.enro.recipes.synthetic

import android.content.Intent
import android.net.Uri
import dev.enro.NavigationContext
import dev.enro.context.root
import dev.enro.platform.activity

internal actual fun launchExternalUrl(context: NavigationContext, url: String) {
    // The root context of an Android navigation tree is hosted by an Activity.
    val activity = context.root().activity
    activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}
