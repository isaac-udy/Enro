package dev.enro.core.controller.usecase

import androidx.compose.runtime.Composable
import dev.enro.core.controller.repository.ComposeEnvironmentRepository
import dev.enro.core.usecase.RenderComposableInEnroEnvironment

internal class RenderComposableInEnroEnvironmentImpl(
    private val composeEnvironmentRepository: ComposeEnvironmentRepository
) : RenderComposableInEnroEnvironment {
    @Composable
    override fun invoke(content: @Composable () -> Unit) {
        composeEnvironmentRepository.Render {
            content()
        }
    }
}