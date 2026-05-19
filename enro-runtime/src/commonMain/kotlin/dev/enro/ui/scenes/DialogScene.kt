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
import dev.enro.ui.get

/** An [NavigationScene.Overlay] that renders an [entry] within a [Dialog]. */
internal data class DialogScene(
    override val key: Any,
    override val previousEntries: List<NavigationDestination<NavigationKey>>,
    override val overlaidEntries: List<NavigationDestination<NavigationKey>>,
    val entry: NavigationDestination<NavigationKey>,
    val dialogProperties: DialogProperties,
) : NavigationScene.Overlay {

    override val entries: List<NavigationDestination<NavigationKey>> = listOf(entry)

    override val content: @Composable () -> Unit = {
        val container = LocalNavigationContainer.current
        Dialog(
            onDismissRequest = {
                container.execute(NavigationOperation.Close(entry.instance))
            },
            properties = dialogProperties,
        ) {
            entry.Content()
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
        val lastEntry = entries.lastOrNull() ?: return null
        val dialogProperties = lastEntry.metadata[DialogPropertiesKey] ?: return null
        return DialogScene(
            key = lastEntry.instance.id,
            previousEntries = entries.dropLast(1),
            overlaidEntries = entries.dropLast(1),
            entry = lastEntry,
            dialogProperties = dialogProperties,
        )
    }

    /**
     * Metadata key under which a destination's [DialogProperties] are
     * stored when it opts into being displayed inside a [Dialog].
     * Mirrors Nav3's `DialogSceneStrategy.DialogKey: NavMetadataKey<DialogProperties>`.
     */
    public object DialogPropertiesKey :
        NavigationDestination.MetadataKey<DialogProperties?>(default = null)

    public companion object
}

/**
 * Marks the destination as one that should be displayed inside a [Dialog].
 */
public fun NavigationDestination.MetadataBuilder<*>.dialog(
    dialogProperties: DialogProperties = DialogProperties(),
) {
    add(DialogSceneStrategy.DialogPropertiesKey, dialogProperties)
}
