package dev.enro.tests.application.samples.travel

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import dev.enro.annotations.NavigationDestination
import dev.enro.asCommonDestination
import dev.enro.close
import dev.enro.desktop.RootWindow
import dev.enro.tests.application.util.EnroTestApplicationMenu
import dev.enro.ui.EmbeddedDestination
import dev.enro.ui.destinations.rootWindowDestination

@NavigationDestination.PlatformOverride(TravelSampleDestination::class)
val travelSampleDesktopWindow = rootWindowDestination<TravelSampleDestination>(
    windowConfiguration = {
        RootWindow.WindowConfiguration(
            title = "Travel Sample",
            state = WindowState(
                width = 600.dp,
                height = 800.dp
            ),
        )
    }
) {
    EnroTestApplicationMenu()

    EmbeddedDestination(
        instance = instance.asCommonDestination(),
        onClosed = navigation::close,
    )
}