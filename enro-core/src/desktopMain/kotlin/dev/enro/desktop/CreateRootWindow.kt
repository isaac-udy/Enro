package dev.enro.desktop

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import dev.enro.NavigationKey

public fun <T: NavigationKey> createRootWindow(
    instance: NavigationKey.Instance<T>,
    windowConfiguration: RootWindow.() -> RootWindow.WindowConfiguration? = { null },
    content: @Composable RootWindowScope.() -> Unit,
): RootWindow {
    return object : RootWindow(instance) {
        val self = this

        init {
            val windowConfiguration = windowConfiguration(this)
            if (windowConfiguration != null) {
                this.windowConfiguration = windowConfiguration
            }
        }

        @Composable
        override fun FrameWindowScope.Content() {
            content(
                RootWindowScope(
                    rootWindow = self,
                    frameWindowScope = this,
                )
            )
        }
    }
}

public open class RootWindowScope internal constructor(
    private val rootWindow: RootWindow,
    private val frameWindowScope: FrameWindowScope,
) : FrameWindowScope by frameWindowScope {

    public constructor(
        rootWindow: RootWindowScope,
    ) : this(rootWindow.rootWindow, rootWindow)

    public val backDispatcher: EnroBackDispatcher
        get() = rootWindow.backDispatcher

    public fun close() {
        rootWindow.close()
    }
}
