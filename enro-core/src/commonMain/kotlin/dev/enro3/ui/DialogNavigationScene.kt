package dev.enro3.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.enro3.NavigationKey
import dev.enro3.ui.DialogNavigationSceneStrategy.Companion.dialog

/** An [OverlayNavigationScene] that renders an [entry] within a [Dialog]. */
internal class DialogNavigationScene(
    override val key: Any,
    override val previousEntries: List<NavigationDestination<out NavigationKey>>,
    override val overlaidEntries: List<NavigationDestination<out NavigationKey>>,
    private val entry: NavigationDestination<out NavigationKey>,
    private val dialogProperties: DialogProperties,
    private val onBack: (count: Int) -> Unit,
) : OverlayNavigationScene {

    override val entries: List<NavigationDestination<out NavigationKey>> = listOf(entry)

    override val content: @Composable (() -> Unit) = {
        Dialog(onDismissRequest = { onBack(1) }, properties = dialogProperties) {
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
public class DialogNavigationSceneStrategy : NavigationSceneStrategy {
    @Composable
    public override fun calculateScene(
        entries: List<NavigationDestination<out NavigationKey>>,
        onBack: (count: Int) -> Unit,
    ): NavigationScene? {
        val lastEntry = entries.lastOrNull()
        val dialogProperties = lastEntry?.metadata?.get(DialogPropertiesKey) as? DialogProperties
        return if (dialogProperties != null) {
            DialogNavigationScene(
                key = lastEntry.instance.id,
                previousEntries = entries.dropLast(1),
                overlaidEntries = entries.dropLast(1),
                entry = lastEntry,
                dialogProperties = dialogProperties,
                onBack = onBack,
            )
        } else null
    }

    public companion object {
        private const val DialogPropertiesKey = "DialogProperties"
        /**
         * Function to create a metadata map with dialog properties to mark this entry as something that
         * should be displayed within a [Dialog].
         *
         * @param dialogProperties properties that should be passed to the containing [Dialog].
         */
        public fun dialog(
            dialogProperties: DialogProperties = DialogProperties()
        ): Pair<String, DialogProperties> = DialogPropertiesKey to dialogProperties
    }
}
