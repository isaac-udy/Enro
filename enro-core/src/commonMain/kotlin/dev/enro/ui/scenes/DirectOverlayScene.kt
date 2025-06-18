package dev.enro.ui.scenes

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneStrategy

/**
 * A [NavigationScene.Overlay] that renders the overlaid content directly on top of the current scene, and
 * leaves it up to the [NavigationDestination] to decide how exactly to render the content.
 *
 * Animations for enter/exit will need to be handled by the [NavigationDestination] itself,
 * as the [DirectOverlayScene] will not apply any animations.
 *
 * If the content in the [DirectOverlayScene] does not prevent the user from interacting with the underlying
 * scene (e.g. by using a Dialog or ModalBottomSheet), it will be possible to click through the overlay
 * and interact with the underlying scene.
 */
public class DirectOverlayScene(
    override val key: Any,
    override val previousEntries: List<NavigationDestination<NavigationKey>>,
    override val overlaidEntries: List<NavigationDestination<NavigationKey>>,
    private val entry: NavigationDestination<NavigationKey>,
) : NavigationScene.Overlay {

    override val entries: List<NavigationDestination<NavigationKey>> = listOf(entry)

    override val content: @Composable (() -> Unit) = {
        entry.content()
    }
}

/**
 * A [NavigationSceneStrategy] that displays entries that have added [dialog] to their metadata
 * within a [Dialog] instance.
 *
 * This strategy should always be added before any non-overlay scene strategies.
 */
public class DirectOverlaySceneStrategy : NavigationSceneStrategy {
    @Composable
    public override fun calculateScene(
        entries: List<NavigationDestination<NavigationKey>>,
    ): NavigationScene? {
        val lastEntry = entries.lastOrNull()

        val directOverlayMetadata = lastEntry?.metadata?.get(DirectOverlayKey) as? Unit
        val isDirectOverlay = directOverlayMetadata != null

        return if (isDirectOverlay) {
            DirectOverlayScene(
                key = lastEntry.instance.id,
                previousEntries = entries.dropLast(1),
                overlaidEntries = entries.dropLast(1),
                entry = lastEntry,
            )
        } else null
    }

    public companion object {
        private const val DirectOverlayKey = "dev.enro.ui.scenes.DirectOverlayKey"

        /**
         * Function to create a metadata map with dialog properties to mark this entry as something that
         * should be displayed within a [Dialog].
         *
         * @param dialogProperties properties that should be passed to the containing [Dialog].
         */
        public fun overlay(): Pair<String, Unit> = DirectOverlayKey to Unit
    }
}

public fun NavigationDestination.MetadataBuilder<*>.directOverlay() {
    add(DirectOverlaySceneStrategy.overlay())
}
