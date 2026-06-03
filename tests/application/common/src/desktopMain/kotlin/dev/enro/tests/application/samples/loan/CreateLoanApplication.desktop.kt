package dev.enro.tests.application.samples.loan

import dev.enro.annotations.NavigationDestination
import dev.enro.asCommonDestination
import dev.enro.close
import dev.enro.complete
import dev.enro.platform.desktop.RootWindow
import dev.enro.ui.EmbeddedDestination
import dev.enro.ui.destinations.rootWindowDestination

@NavigationDestination.PlatformOverride(CreateLoanSampleDestination::class)
val createLoanApplicationWindow = rootWindowDestination<CreateLoanSampleDestination>(
    windowConfiguration = {
        RootWindow.WindowConfiguration(
            title = "Create Loan Application",
            onCloseRequest = { navigation.close() },
        )
    }
) {
    EmbeddedDestination(
        instance = instance.asCommonDestination(),
        onClosed = { navigation.close() },
        onCompleted = { result -> navigation.complete(result) }
    )
}