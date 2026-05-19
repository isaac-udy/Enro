package dev.enro.ui

import androidx.compose.runtime.Immutable
import androidx.navigationevent.NavigationEventInfo

/**
 * A snapshot of the active [NavigationScene] for use with the navigation
 * event system. Mirrors Nav3's `SceneInfo<T>`: a typed [NavigationEventInfo]
 * that lets back handlers (predictive or otherwise) reason about which scene
 * is currently rendering.
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
