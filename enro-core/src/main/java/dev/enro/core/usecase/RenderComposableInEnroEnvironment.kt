package dev.enro.core.usecase

import androidx.compose.runtime.Composable

internal interface RenderComposableInEnroEnvironment {
    @Composable
    operator fun invoke(content: @Composable () -> Unit)
}