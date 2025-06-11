package dev.enro.desktop

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope

public fun createRootWindow(
    content: @Composable RootWindowScope.() -> Unit,
): RootWindow {
    return object : RootWindow() {
        val self = this

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

public class RootWindowScope internal constructor(
    private val rootWindow: RootWindow,
    private val frameWindowScope: FrameWindowScope,
) : FrameWindowScope by frameWindowScope {

    public val backDispatcher: EnroBackDispatcher
        get() = rootWindow.backDispatcher

    public fun close() {
        rootWindow.close()
    }
}
