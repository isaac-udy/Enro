package dev.enro.core.controller

import androidx.compose.runtime.Composable
import dev.enro.controller.NavigationModule
import dev.enro.ui.decorators.navigationDestinationDecorator

public fun NavigationModule.BuilderScope.composeEnvironment(
    block: @Composable (content: @Composable () -> Unit) -> Unit
) {
    decorator {
        navigationDestinationDecorator { destination ->
            block {
                destination.content()
            }
        }
    }
}