// Snippets for docs/ghpages/docs/advanced/synthetic-destinations.md
package dev.enro.docs.advanced.syntheticdestinations.android

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.ui.destinations.syntheticDestination
import kotlinx.serialization.Serializable

@Serializable
data class OpenExternalUrl(val url: String) : NavigationKey

private fun NavigationContext.findActivity(): Activity = TODO()

// region docs:synthetic-destinations/side-effect-android
@NavigationDestination(OpenExternalUrl::class)
val openExternalUrl = syntheticDestination<OpenExternalUrl> {
    sideEffect {
        // findActivity() is your own helper, not an Enro API.
        val activity = context.findActivity()
        activity.startActivity(Intent(ACTION_VIEW, Uri.parse(key.url)))
    }
}
// endregion
