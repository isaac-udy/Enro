package dev.enro3.ui.scenes

import androidx.compose.runtime.Composable
import dev.enro3.NavigationKey
import dev.enro3.ui.NavigationDestination
import dev.enro3.ui.NavigationScene
import dev.enro3.ui.NavigationSceneStrategy

@Composable
internal fun NavigationSceneStrategy.calculateSceneWithSinglePaneFallback(
    entries: List<NavigationDestination<out NavigationKey>>,
    onBack: (count: Int) -> Unit,
): NavigationScene = calculateScene(entries, onBack) ?: SinglePaneScene().calculateScene(entries, onBack)
