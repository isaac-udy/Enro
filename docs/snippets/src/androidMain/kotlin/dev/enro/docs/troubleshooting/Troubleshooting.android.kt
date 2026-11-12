// Snippets for docs/ghpages/docs/troubleshooting.md
package dev.enro.docs.troubleshooting

import android.app.Application

// region docs:troubleshooting/install
// Android
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MyComponent.installNavigationController(this)   // <- this must run first
    }
}
// endregion
