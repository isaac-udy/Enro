// Snippets for docs/ghpages/docs/advanced/path-bindings.md
package dev.enro.docs.advanced.pathbindings

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.enro.asBackstack
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.path.getNavigationKeyFromPath
import dev.enro.ui.LocalNavigationContext
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer

// region docs:path-bindings/android-deep-link
class MainActivity : ComponentActivity() {
    private var pendingDeepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingDeepLink = intent.dataString
        setContent {
            val container = rememberNavigationContainer(
                backstack = backstackOf(Home.asInstance()),
            )
            val context = LocalNavigationContext.current
            LaunchedEffect(pendingDeepLink) {
                val path = pendingDeepLink ?: return@LaunchedEffect
                pendingDeepLink = null
                val key = context.getNavigationKeyFromPath(path) ?: return@LaunchedEffect
                container.updateBackstack { (it + key.asInstance()).asBackstack() }
            }
            NavigationDisplay(state = container)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingDeepLink = intent.dataString
    }
}
// endregion
