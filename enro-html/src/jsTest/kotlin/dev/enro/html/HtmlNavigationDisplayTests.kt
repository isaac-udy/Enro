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
    object ConfirmKey : NavigationKey.WithResult<Boolean>

    @Serializable
    object FadingOverlayKey : NavigationKey
}
