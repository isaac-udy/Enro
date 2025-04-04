package dev.enro.core.controller.usecase

import androidx.compose.runtime.Composable
import dev.enro.core.controller.repository.ComposeEnvironmentRepository

internal class ComposeEnvironment(
    private val repository: ComposeEnvironmentRepository
) {
    @Composable
    operator fun invoke(
        content: @Composable () -> Unit
    ) {
        repository.Render(content)
    }
}