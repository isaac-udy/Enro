package dev.enro.core.controller.container

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

internal typealias ComposeEnvironment = @Composable (@Composable () -> Unit) -> Unit

internal class ComposeEnvironmentContainer {
    private val composeEnvironment: MutableState<ComposeEnvironment> = mutableStateOf({ content -> content() })

    internal fun setComposeEnvironment(environment: ComposeEnvironment) {
        composeEnvironment.value = environment
    }

    @Composable
    internal fun Render(content: @Composable () -> Unit) {
        composeEnvironment.value.invoke(content)
    }
}