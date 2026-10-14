package dev.enro.html

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import dev.enro.NavigationKey
import dev.enro.ui.LocalNavigationContainer
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationScene
import dev.enro.ui.NavigationSceneStrategy
import dev.enro.ui.SceneStrategyScope
import dev.enro.ui.get
import org.jetbrains.compose.web.dom.ElementBuilder
import org.jetbrains.compose.web.dom.TagElement
import org.w3c.dom.HTMLDialogElement
import org.w3c.dom.HTMLElement

/**
 * How an [htmlDialog] destination is dismissed.
 *
 * @param dismissOnEscape whether Escape requests the destination closes.
 * @param dismissOnBackdropClick whether a click outside the dialog's box requests the
 *   destination closes.
 */
public class HtmlDialogProperties(
    public val dismissOnEscape: Boolean = true,
    public val dismissOnBackdropClick: Boolean = true,
)

/**
 * Marks the destination as one that [HtmlNavigationDisplay] renders inside a modal `<dialog>`
 * element. Style the dialog and its backdrop with CSS (`dialog` and `dialog::backdrop`). A CSS
 * transition on the closed dialog runs before the destination leaves the DOM.
 */
public fun NavigationDestination.MetadataBuilder<*>.htmlDialog(
    properties: HtmlDialogProperties = HtmlDialogProperties(),
) {
    add(HtmlDialogSceneStrategy.HtmlDialogPropertiesKey, properties)
}

/**
 * A [NavigationSceneStrategy] that displays entries that have added [htmlDialog] to their
 * metadata inside a modal `<dialog>` element.
 *
 * This strategy should always be added before any non-overlay scene strategies.
 */
public class HtmlDialogSceneStrategy : NavigationSceneStrategy {
    @Composable
    public override fun SceneStrategyScope.calculateScene(
        entries: List<NavigationDestination<NavigationKey>>,
    ): NavigationScene? {
        val lastEntry = entries.lastOrNull() ?: return null
        val properties = lastEntry.metadata[HtmlDialogPropertiesKey] ?: return null
        return HtmlDialogScene(
            key = lastEntry.instance.id,
            previousEntries = entries.dropLast(1),
            overlaidEntries = entries.dropLast(1),
            entry = lastEntry,
            properties = properties,
        )
    }

    public object HtmlDialogPropertiesKey :
        NavigationDestination.MetadataKey<HtmlDialogProperties?>(
            default = null,
            name = "dev.enro.html.HtmlDialogSceneStrategy.HtmlDialogPropertiesKey",
        )

    public companion object
}

internal data class HtmlDialogScene(
    override val key: Any,
    override val previousEntries: List<NavigationDestination<NavigationKey>>,
    override val overlaidEntries: List<NavigationDestination<NavigationKey>>,
    val entry: NavigationDestination<NavigationKey>,
    val properties: HtmlDialogProperties,
) : NavigationScene.Overlay {

    override val entries: List<NavigationDestination<NavigationKey>> = listOf(entry)

    override val content: @Composable () -> Unit = {
        Render(visible = true, onElement = {})
    }

    @Composable
    fun Render(
        visible: Boolean,
        onElement: (HTMLElement?) -> Unit,
    ) {
        val container = LocalNavigationContainer.current
        val currentVisible by rememberUpdatedState(visible)
        var dialog by remember { mutableStateOf<HTMLDialogElement?>(null) }
        var closedByBrowser by remember { mutableIntStateOf(0) }

        TagElement(
            elementBuilder = DialogElementBuilder,
            applyAttrs = {
                ref { element ->
                    dialog = element
                    onElement(element)
                    element.showModal()
                    onDispose {
                        dialog = null
                        onElement(null)
                        if (element.open) element.close()
                    }
                }
                addEventListener("cancel") { event ->
                    event.preventDefault()
                    if (properties.dismissOnEscape) container.requestClose(entry)
                }
                // Chrome closes a dialog without a cancelable "cancel" event when Escape is
                // pressed twice with no user activation in between.
                addEventListener("close") {
                    if (dialog == null || !currentVisible) return@addEventListener
                    if (properties.dismissOnEscape) container.requestClose(entry)
                    closedByBrowser++
                }
                onClick { event ->
                    val element = dialog ?: return@onClick
                    if (!properties.dismissOnBackdropClick || event.target != element) return@onClick
                    val bounds = element.getBoundingClientRect()
                    val outside = event.clientX < bounds.left || event.clientX > bounds.right ||
                        event.clientY < bounds.top || event.clientY > bounds.bottom
                    if (outside) container.requestClose(entry)
                }
            },
        ) {
            entry.Content()
        }

        SideEffect {
            val element = dialog ?: return@SideEffect
            if (!visible && element.open) element.close()
        }
        LaunchedEffect(closedByBrowser) {
            val element = dialog ?: return@LaunchedEffect
            if (currentVisible && !element.open) element.showModal()
        }
    }
}

private val DialogElementBuilder = ElementBuilder.createBuilder<HTMLDialogElement>("dialog")
