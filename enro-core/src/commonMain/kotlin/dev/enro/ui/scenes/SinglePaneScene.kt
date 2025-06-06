package dev.enro.ui.scenes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneStrategy

public class SinglePaneSceneStrategy : NavigationSceneStrategy {
    @Composable
    override fun calculateScene(
        entries: List<NavigationDestination<NavigationKey>>,
    ): NavigationScene {
       return remember(entries) {
           object : NavigationScene {
               override val entries: List<NavigationDestination<NavigationKey>> = entries.takeLast(1)
               override val key: Any = SinglePaneSceneStrategy::class to entries.map { it.instance.id }
               override val previousEntries: List<NavigationDestination<NavigationKey>> = entries.dropLast(1)
               override val content: @Composable (() -> Unit) = {
                   val entries = this.entries
                   if (entries.isNotEmpty()) {
                       entries.single().content()
                   }
               }
           }
       }
    }
}

