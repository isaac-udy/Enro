package dev.enro.recipes

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.recipes.deeplink.openDeepLink
import dev.enro.ui.LocalNavigationContext
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer

class RecipesActivity : AppCompatActivity() {
    // The path of an `enrorecipes://app/...` link that has not been opened yet.
    private var pendingDeepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // After a configuration change the launch intent is delivered again;
        // the link it carries has already been handled.
        if (savedInstanceState == null) {
            pendingDeepLink = intent.deepLinkPath()
        }
        setContent {
            MaterialTheme {
                val container = rememberNavigationContainer(
                    backstack = backstackOf(SelectRecipe.asInstance()),
                )
                val context = LocalNavigationContext.current
                LaunchedEffect(pendingDeepLink) {
                    val path = pendingDeepLink ?: return@LaunchedEffect
                    pendingDeepLink = null
                    // See the Handling a Deep Link recipe.
                    container.openDeepLink(context, path)
                }
                NavigationDisplay(state = container)
            }
        }
        applyInsetsForContentView()
    }

    // A link that arrives while the Activity is already running.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingDeepLink = intent.deepLinkPath()
    }
}

/** `enrorecipes://app/users/alice?tab=1` becomes `/users/alice?tab=1`. */
private fun Intent.deepLinkPath(): String? {
    val uri = data?.takeIf { it.scheme == "enrorecipes" } ?: return null
    val path = uri.path?.takeIf { it.isNotEmpty() } ?: "/"
    return uri.query?.let { "$path?$it" } ?: path
}

private fun AppCompatActivity.applyInsetsForContentView() {
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = insets.top
            leftMargin = insets.left
            bottomMargin = insets.bottom
            rightMargin = insets.right
        }
        WindowInsetsCompat.CONSUMED
    }
}
