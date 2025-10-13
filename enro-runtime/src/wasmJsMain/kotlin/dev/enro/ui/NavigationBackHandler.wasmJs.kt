package dev.enro.ui

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

@Composable
internal actual fun NavigationBackHandler(
    enabled: Boolean,
    onBack: suspend (Flow<NavigationBackEvent>) -> Unit,
) {
    TODO()
}