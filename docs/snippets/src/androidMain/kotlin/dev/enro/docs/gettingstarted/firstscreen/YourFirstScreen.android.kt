// Android snippets for docs/ghpages/docs/getting-started/your-first-screen.md
// and the Android section of installation.md, which shows the same code.
package dev.enro.docs.gettingstarted.firstscreen

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer

// region docs:your-first-screen/application
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MyComponent.installNavigationController(this)
    }
}
// endregion

// region docs:your-first-screen/activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val container = rememberNavigationContainer(
                backstack = backstackOf(Home.asInstance()),
            )
            NavigationDisplay(state = container)
        }
    }
}
// endregion
