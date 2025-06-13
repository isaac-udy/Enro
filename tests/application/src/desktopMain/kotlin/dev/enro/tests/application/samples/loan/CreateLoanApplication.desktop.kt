package dev.enro.tests.application.samples.loan

import dev.enro.annotations.NavigationDestination
import dev.enro.asCommonDestination
import dev.enro.close
import dev.enro.desktop.RootWindow
import dev.enro.ui.EmbeddedDestination
import dev.enro.ui.destinations.rootWindowDestination

@NavigationDestination.PlatformOverride(CreateLoanSample::class)
val createLoanApplicationWindow = rootWindowDestination<CreateLoanSample>(
    windowConfiguration = {
        RootWindow.WindowConfiguration(
            title = "Create Loan Application",
            onCloseRequest = { navigation.close() },
        )
    }
) {
    EmbeddedDestination(
        instance = instance.asCommonDestination(),
        onClosed = { navigation.close() }
    )
}