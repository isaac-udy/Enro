package dev.enro.ui

import androidx.compose.runtime.Immutable
import androidx.navigationevent.NavigationEventInfo

/**
 * The [NavigationEventInfo] that `NavigationDisplay` registers with the navigation event
 * system, so that a back handler can read the current [NavigationScene]. Navigation 3
 * equivalent: `SceneInfo`.
 */
@Immutable
public class NavigationSceneInfo(public val scene: NavigationScene) : NavigationEventInfo() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NavigationSceneInfo) return false
        return scene == other.scene
    }

    override fun hashCode(): Int = scene.hashCode()

    override fun toString(): String = "NavigationSceneInfo(scene=$scene)"
}
