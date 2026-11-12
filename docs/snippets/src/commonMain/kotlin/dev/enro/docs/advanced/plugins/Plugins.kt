// Snippets for docs/ghpages/docs/advanced/plugins.md
package dev.enro.docs.advanced.plugins

import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationComponent
import dev.enro.controller.NavigationComponentConfiguration
import dev.enro.controller.createNavigationModule
import dev.enro.plugin.NavigationPlugin
import kotlin.time.Clock

class MyAnalyticsPlugin : NavigationPlugin()

// region docs:plugins/install
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        plugin(MyAnalyticsPlugin())
        plugin(OpenedTimestampPlugin())
    }
)
// endregion

// region docs:plugins/opened-timestamp
object OpenedAt : NavigationKey.MetadataKey<Long?>(default = null)

class OpenedTimestampPlugin : NavigationPlugin() {
    override fun onOpened(navigationHandle: NavigationHandle<*>) {
        val instance = navigationHandle.instance
        if (instance.metadata.get(OpenedAt) == null) {
            instance.metadata.set(OpenedAt, Clock.System.now().toEpochMilliseconds())
        }
    }
}
// endregion

fun readOpenedAt(navigation: NavigationHandle<*>) {
    // region docs:plugins/read
    val openedAt = navigation.instance.metadata.get(OpenedAt)
    // endregion
}
