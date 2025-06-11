package dev.enro.tests.application.samples.loan

import dev.enro.annotations.NavigationDestination
import dev.enro.asCommonDestination
import dev.enro.desktop.RootWindow
import dev.enro.ui.EmbeddedDestination
import dev.enro.ui.destinations.rootWindowDestination

@NavigationDestination.PlatformOverride(CreateLoanApplication::class)
val createLoanApplicationWindow = rootWindowDestination<CreateLoanApplication>(
    windowConfiguration = {
        RootWindow.WindowConfiguration(
            title = "Create Loan Application",
            onCloseRequest = { close() },
        )
    }
) {
    EmbeddedDestination(
        instance = instance.asCommonDestination(),
        onClosed = { close() }
    )
}