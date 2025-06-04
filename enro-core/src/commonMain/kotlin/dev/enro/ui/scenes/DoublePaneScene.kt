package dev.enro.ui.scenes

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import dev.enro.NavigationKey
import dev.enro.ui.LocalNavigationAnimatedVisibilityScope
import dev.enro.ui.LocalNavigationSharedTransitionScope
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneStrategy

public class DoublePaneScene : NavigationSceneStrategy {
    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    override fun calculateScene(
        entries: List<NavigationDestination<out NavigationKey>>,
    ): NavigationScene {
        return object : NavigationScene {

            override val entries: List<NavigationDestination<out NavigationKey>> =
                if (entries.size >= 2) entries.takeLast(2) else listOf(entries.last())

            override val key: Any = DoublePaneScene::class to entries.map { it.instance.id }

            override val previousEntries: List<NavigationDestination<out NavigationKey>> =
                entries.dropLast(1)

            override val content: @Composable (() -> Unit) = {
                val entries = this.entries.toList()
                val width = with(LocalDensity.current) {
                    LocalWindowInfo.current.containerSize.toSize().width.toDp()
                }
                Row {
                    with(LocalNavigationSharedTransitionScope.current) {
                        if (entries.size == 2 && width > 600.dp) {
                            // Render both destinations side by side or in some layout
                            Box(
                                modifier = Modifier.Companion
                                    .sharedElement(
                                        rememberSharedContentState(key = entries.first().instance.id),
                                        animatedVisibilityScope = LocalNavigationAnimatedVisibilityScope.current,
                                    )
                                    .weight(1f)

                            ) { entries.first().content() }

                            Box(
                                modifier = Modifier.Companion
                                    .sharedElement(
                                        rememberSharedContentState(key = entries.last().instance.id),
                                        animatedVisibilityScope = LocalNavigationAnimatedVisibilityScope.current,
                                    )
                                    .weight(1f)
                            ) { entries.last().content() }
                        } else {
                            Box(
                                modifier = Modifier.Companion
                                    .sharedElement(
                                        rememberSharedContentState(key = entries.first().instance.id),
                                        animatedVisibilityScope = LocalNavigationAnimatedVisibilityScope.current,
                                    )
                                    .weight(1f)
                            ) { entries.first().content() }
                        }
                    }
                }
            }
        }
    }
}