package dev.enro.ui.scenes

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneStrategy

@Composable
internal fun NavigationSceneStrategy.calculateSceneWithSinglePaneFallback(
    entries: List<NavigationDestination<out NavigationKey>>,
    onBack: (count: Int) -> Unit,
): NavigationScene = calculateScene(entries, onBack) ?: SinglePaneScene().calculateScene(entries, onBack)
