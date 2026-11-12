// iOS snippets for docs/ghpages/docs/getting-started/installation.md
package dev.enro.docs.gettingstarted.installation

import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.platform.EnroUIViewController
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import platform.UIKit.UIViewController

// region docs:installation/ios
fun MainViewController(): UIViewController = EnroUIViewController {
    val container = rememberNavigationContainer(
        backstack = backstackOf(Home.asInstance()),
    )
    NavigationDisplay(state = container)
}
// endregion
