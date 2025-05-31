package dev.enro3.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.enro3.NavigationKey


public interface NavigationScene {
    public val key: Any
    public val entries: List<NavigationDestination<out NavigationKey>>
    public val previousEntries: List<NavigationDestination<out NavigationKey>>
    public val content: @Composable () -> Unit
}

public interface NavigationSceneStrategy {
    @Composable
    public fun calculateScene(
        entries: List<NavigationDestination<out NavigationKey>>,
        onBack: (count: Int) -> Unit
    ): NavigationScene?
}

public class SinglePaneScene : NavigationSceneStrategy {
    @Composable
    override fun calculateScene(
        entries: List<NavigationDestination<out NavigationKey>>,
        onBack: (Int) -> Unit
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


public class DoublePaneScene : NavigationSceneStrategy {
    @Composable
    override fun calculateScene(
        entries: List<NavigationDestination<out NavigationKey>>,
        onBack: (Int) -> Unit
    ): NavigationScene {
        return object : NavigationScene {

            override val entries: List<NavigationDestination<out NavigationKey>> = entries.takeLast(2)
            override val key: Any = DoublePaneScene::class to entries.map { it.instance.id }

            override val previousEntries: List<NavigationDestination<out NavigationKey>> = entries.dropLast(1)
            override val content: @Composable (() -> Unit) = {
                Column {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        entries[0].content()
                    }
                    if (entries.size > 1) {
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            entries[1].content()
                        }
                    }
                }
            }
        }
    }
}
