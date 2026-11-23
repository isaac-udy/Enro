package dev.enro.html

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.complete
import dev.enro.configure
import dev.enro.controller.createNavigationModule
import dev.enro.open
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.test.installNavigationModule
import dev.enro.ui.InstallWebHistoryPlugin
import dev.enro.ui.navigationDestinationContent
import dev.enro.ui.rememberNavigationContainer
import dev.enro.ui.scenes.directOverlayWithFade
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.EventInit
import org.w3c.dom.HTMLDialogElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.MouseEventInit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HtmlNavigationDisplayTests {

    @Test
    fun opensAndClosesDestinations() = runHtmlTest {
        installTestDestinations()
        setTestContent()

        assertNotNull(element("#home"))

        click("#open-detail")
        awaitUntil("the detail destination replaces home") {
            element("#detail-1") != null && element("#home") == null
        }

        click("#close-detail")
        awaitUntil("home is shown again") { element("#home") != null && element("#detail-1") == null }
    }

    @Test
    fun htmlDialogRendersInAnOpenModalDialogAndReturnsItsResult() = runHtmlTest {
        installTestDestinations()
        setTestContent()

        click("#open-confirm")
        awaitUntil("the confirm dialog opens") { element("dialog #confirm") != null }
        val dialog = requireElement("dialog") as HTMLDialogElement
        assertTrue(dialog.open)
        assertNotNull(element("#home"), "The destination under the dialog stays rendered")

        click("#confirm-yes")
        awaitUntil("the dialog leaves the DOM") { element("dialog") == null }
        assertEquals("result=true", requireElement("#home-result").textContent)
    }

    @Test
    fun cancelEventOnHtmlDialogRequestsClose() = runHtmlTest {
        installTestDestinations()
        setTestContent()

        click("#open-confirm")
        awaitUntil("the confirm dialog opens") { element("dialog") != null }
        requireElement("dialog").dispatchEvent(Event("cancel", EventInit(cancelable = true)))

        awaitUntil("the dialog leaves the DOM") { element("dialog") == null }
        assertEquals("result=closed", requireElement("#home-result").textContent)
    }

    @Test
    fun backdropClickOnHtmlDialogRequestsClose() = runHtmlTest {
        installTestDestinations()
        setTestContent()

        click("#open-confirm")
        awaitUntil("the confirm dialog opens") { element("dialog") != null }
        val dialog = requireElement("dialog")
        val bounds = dialog.getBoundingClientRect()

        dialog.dispatchEvent(
            MouseEvent(
                "click",
                MouseEventInit(
                    bubbles = true,
                    clientX = (bounds.left + bounds.width / 2).toInt(),
                    clientY = (bounds.top + bounds.height / 2).toInt(),
                ),
            )
        )
        awaitFrame()
        awaitFrame()
        assertNotNull(element("dialog"), "A click inside the dialog box keeps it open")

        dialog.dispatchEvent(MouseEvent("click", MouseEventInit(bubbles = true, clientX = 0, clientY = 0)))
        awaitUntil("the dialog leaves the DOM") { element("dialog") == null }
    }

    @Test
    fun fadingDirectOverlayStaysInTheDomUntilItsFadeEnds() = runHtmlTest {
        installTestDestinations()
        setTestContent()

        fun overlayOpacity() = (element("#overlay")?.parentElement as HTMLElement?)?.style?.opacity

        click("#open-overlay")
        awaitUntil("the overlay is fully shown") { overlayOpacity() == "1" }

        click("#close-overlay")
        awaitUntil("the overlay starts fading out") { overlayOpacity() == "0" }
        assertNotNull(element("#overlay"), "The overlay is kept while it fades out")
        awaitUntil("the overlay leaves the DOM") { element("#overlay") == null }
    }

    @Test
    fun browserBackAndForwardMoveThroughTheBackstack() = runHtmlTest {
        installTestDestinations()
        val stateBeforeContent = window.history.state
        setTestContent(withWebHistory = true)
        awaitUntil("the history plugin records the initial backstack") {
            window.history.state != stateBeforeContent
        }
        val initialHistoryLength = window.history.length

        click("#open-detail")
        awaitUntil("the detail destination is shown") { element("#detail-1") != null }
        awaitUntil("browser history records the detail destination") {
            window.history.length > initialHistoryLength
        }

        window.history.back()
        awaitUntil("back shows home") { element("#home") != null && element("#detail-1") == null }

        window.history.forward()
        awaitUntil("forward shows the detail destination") { element("#detail-1") != null }
    }

    @Test
    fun browserBackThroughAnOnCloseRequestedCallbackKeepsTheForwardEntry() = runHtmlTest {
        installTestDestinations()
        openGuardedWithWebHistory()

        window.history.back()
        awaitUntil("the callback closes the guarded destination") {
            element("#detail-1") != null && element("#guarded") == null
        }
        awaitHistoryEntryWithout("GuardedKey")

        window.history.forward()
        awaitUntil("forward shows the guarded destination again") { element("#guarded") != null }
    }

    @Test
    fun browserBackKeepsADestinationWhoseCallbackDoesNotClose() = runHtmlTest {
        installTestDestinations()
        openGuardedWithWebHistory()
        click("#block-close")

        window.history.back()
        awaitUntil("the callback receives the close request") {
            element("#guarded-requests")?.textContent == "requests=1"
        }
        awaitFrame()
        awaitFrame()
        assertNotNull(element("#guarded"), "A callback that does not close keeps the destination")

        awaitHistoryEntryWith("GuardedKey")

        click("#allow-close")
        window.history.back()
        awaitUntil("the second back closes the guarded destination") {
            element("#detail-1") != null && element("#guarded") == null
        }
        awaitHistoryEntryWithout("GuardedKey")
        window.history.forward()
        awaitUntil("forward shows the guarded destination again") { element("#guarded") != null }
    }

    private suspend fun HtmlTestScope.openGuardedWithWebHistory() {
        val stateBeforeContent = window.history.state
        setTestContent(withWebHistory = true)
        awaitUntil("the history plugin records the initial backstack") {
            window.history.state != stateBeforeContent
        }
        // A detail destination under the guarded one keeps the close off history's first
        // entry, where the plugin already keeps forward entries.
        click("#open-detail")
        awaitHistoryEntryWith("DetailKey")
        click("#open-guarded")
        awaitUntil("the guarded destination is shown") { element("#guarded") != null }
        awaitHistoryEntryWith("GuardedKey")
    }

    // The plugin writes browser history after the DOM changes. Every test in the run shares
    // one browser history, so a push does not always increase history.length.
    private suspend fun HtmlTestScope.awaitHistoryEntryWith(keyName: String) {
        awaitUntil("the browser history entry contains $keyName") { historyState().contains(keyName) }
        awaitFrame()
    }

    private suspend fun HtmlTestScope.awaitHistoryEntryWithout(keyName: String) {
        awaitUntil("the browser history entry does not contain $keyName") { !historyState().contains(keyName) }
        awaitFrame()
    }

    private fun historyState(): String = window.history.state as? String ?: ""

    private fun HtmlTestScope.setTestContent(withWebHistory: Boolean = false) {
        setContent {
            EnroHtmlContent {
                val container = rememberNavigationContainer(
                    backstack = backstackOf(HomeKey.asInstance()),
                )
                if (withWebHistory) InstallWebHistoryPlugin(container)
                HtmlNavigationDisplay(container)
            }
        }
    }

    private fun installTestDestinations() {
        installNavigationModule(
            createNavigationModule {
                destination<HomeKey>(
                    navigationDestinationContent {
                        var result by remember { mutableStateOf("none") }
                        val confirm = registerForNavigationResult<Boolean>(
                            onClosed = { result = "closed" },
                            onCompleted = { result = it.toString() },
                        )
                        Div({ id("home") }) {
                            Div({ id("home-result") }) { Text("result=$result") }
                            Button({
                                id("open-detail")
                                onClick { navigation.open(DetailKey("1")) }
                            }) { Text("Detail") }
                            Button({
                                id("open-confirm")
                                onClick { confirm.open(ConfirmKey) }
                            }) { Text("Confirm") }
                            Button({
                                id("open-overlay")
                                onClick { navigation.open(FadingOverlayKey) }
                            }) { Text("Overlay") }
                        }
                    }
                )
                destination<DetailKey>(
                    navigationDestinationContent {
                        Div({ id("detail-${navigation.key.id}") }) {
                            Button({
                                id("close-detail")
                                onClick { navigation.close() }
                            }) { Text("Close") }
                            Button({
                                id("open-guarded")
                                onClick { navigation.open(GuardedKey) }
                            }) { Text("Guarded") }
                        }
                    }
                )
                destination<GuardedKey>(
                    navigationDestinationContent {
                        var allowClose by remember { mutableStateOf(true) }
                        var requests by remember { mutableStateOf(0) }
                        navigation.configure {
                            onCloseRequested {
                                requests++
                                if (allowClose) close()
                            }
                        }
                        Div({ id("guarded") }) {
                            Div({ id("guarded-requests") }) { Text("requests=$requests") }
                            Button({
                                id("block-close")
                                onClick { allowClose = false }
                            }) { Text("Block close") }
                            Button({
                                id("allow-close")
                                onClick { allowClose = true }
                            }) { Text("Allow close") }
                        }
                    }
                )
                destination<ConfirmKey>(
                    navigationDestinationContent(metadata = { htmlDialog() }) {
                        Div({ id("confirm") }) {
                            Button({
                                id("confirm-yes")
                                onClick { navigation.complete(true) }
                            }) { Text("Yes") }
                        }
                    }
                )
                destination<FadingOverlayKey>(
                    navigationDestinationContent(metadata = { directOverlayWithFade(durationMillis = 150) }) {
                        Div({ id("overlay") }) {
                            Button({
                                id("close-overlay")
                                onClick { navigation.close() }
                            }) { Text("Close") }
                        }
                    }
                )
            }
        )
    }

    @Serializable
    object HomeKey : NavigationKey

    @Serializable
    data class DetailKey(val id: String) : NavigationKey

    @Serializable
    object GuardedKey : NavigationKey

    @Serializable
    object ConfirmKey : NavigationKey.WithResult<Boolean>

    @Serializable
    object FadingOverlayKey : NavigationKey
}
