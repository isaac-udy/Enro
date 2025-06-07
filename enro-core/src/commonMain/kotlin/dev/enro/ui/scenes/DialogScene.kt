package dev.enro.ui.scenes

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.ui.LocalNavigationContainer
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneStrategy
import dev.enro.ui.scenes.DialogSceneStrategy.Companion.dialog

/** An [NavigationScene.Overlay] that renders an [entry] within a [Dialog]. */
internal class DialogScene(
    override val key: Any,
    override val previousEntries: List<NavigationDestination<out NavigationKey>>,
    override val overlaidEntries: List<NavigationDestination<out NavigationKey>>,
    private val entry: NavigationDestination<out NavigationKey>,
    private val dialogProperties: DialogProperties,
) : NavigationScene.Overlay {

    override val entries: List<NavigationDestination<out NavigationKey>> = listOf(entry)

    override val content: @Composable (() -> Unit) = {
        val container = LocalNavigationContainer.current
        Dialog(
            onDismissRequest = {
                container.execute(NavigationOperation.Close(entry.instance))
            },
            properties = dialogProperties,
        ) {
            entry.content()
        }
    }
}

/**
 * A [NavigationSceneStrategy] that displays entries that have added [dialog] to their metadata
 * within a [Dialog] instance.
 *
 * This strategy should always be added before any non-overlay scene strategies.
 */
public class DialogSceneStrategy : NavigationSceneStrategy {
    @Composable
    public override fun calculateScene(
        entries: List<NavigationDestination<NavigationKey>>,
    ): NavigationScene? {
        val lastEntry = entries.lastOrNull()
        val dialogProperties = lastEntry?.metadata?.get(DialogPropertiesKey) as? DialogProperties

        return if (dialogProperties != null) {
            DialogScene(
                key = lastEntry.instance.id,
                previousEntries = entries.dropLast(1),
                overlaidEntries = entries.dropLast(1),
                entry = lastEntry,
                dialogProperties = dialogProperties,
            )
        } else null
    }

    public companion object Companion {
        private const val DialogPropertiesKey = "dev.enro.ui.scenes.DialogProperties"

        /**
         * Function to create a metadata map with dialog properties to mark this entry as something that
         * should be displayed within a [Dialog].
         *
         * @param dialogProperties properties that should be passed to the containing [Dialog].
         */
        public fun dialog(
            dialogProperties: DialogProperties = DialogProperties(),
        ): Pair<String, DialogProperties> = DialogPropertiesKey to dialogProperties
    }
}
