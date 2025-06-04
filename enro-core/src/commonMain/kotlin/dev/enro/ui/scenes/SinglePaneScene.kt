package dev.enro.ui.scenes

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneStrategy

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

