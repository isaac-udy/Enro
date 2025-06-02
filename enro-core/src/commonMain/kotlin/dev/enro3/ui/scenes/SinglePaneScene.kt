package dev.enro3.ui.scenes

import androidx.compose.runtime.Composable
import dev.enro3.NavigationKey
import dev.enro3.ui.NavigationDestination
import dev.enro3.ui.NavigationScene
import dev.enro3.ui.NavigationSceneStrategy

public class SinglePaneScene : NavigationSceneStrategy {
    @Composable
    override fun calculateScene(
        entries: List<NavigationDestination<out NavigationKey>>,
        onBack: (Int) -> Unit,
    ): NavigationScene {
        return object : NavigationScene {

            override val entries: List<NavigationDestination<out NavigationKey>> = listOf(entries.last())
            override val key: Any = SinglePaneScene::class to entries.map { it.instance.id }

            override val previousEntries: List<NavigationDestination<out NavigationKey>> = entries.dropLast(1)
            override val content: @Composable (() -> Unit) = {
                this.entries.single().content()
            }
        }
    }
}

