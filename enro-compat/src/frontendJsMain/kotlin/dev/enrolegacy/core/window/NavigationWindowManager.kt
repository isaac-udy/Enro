package dev.enrolegacy.core.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.asPresent
import dev.enro.core.controller.NavigationController
import dev.enro.core.plugins.EnroPlugin
import dev.enro.destination.compose.ComposableNavigationBinding
import dev.enro.destination.flow.ManagedFlowNavigationBinding
import dev.enro.destination.web.HostComposableInWebWindow
import dev.enro.destination.web.WebWindow
import dev.enro.destination.web.WebWindowNavigationBinding
import kotlinx.browser.sessionStorage
import kotlinx.browser.window
import org.w3c.dom.get
import org.w3c.dom.set
import org.w3c.dom.url.URLSearchParams


public actual class NavigationWindowManager actual constructor(
    private val controller: NavigationController,
) : EnroPlugin() {

    private lateinit var activeWebWindow: WebWindow

    public actual fun open(instruction: AnyOpenInstruction) {
        if (!::activeWebWindow.isInitialized) {
            val search = URLSearchParams(window.location.search.toJsString())
            val instructionForRoot = search.get("instruction")
                ?.let { instructionId ->
                    println("LOADING: $instructionId")
                    val loaded = sessionStorage.get(instructionId)
                    println("LOADED: $loaded")
                    sessionStorage.removeItem(instructionId)
                    return@let loaded
                }
                ?.let { serializedInstruction ->
                    runCatching {
                        NavigationController.jsonConfiguration.decodeFromString<NavigationInstruction.Open<NavigationDirection>>(
                            serializedInstruction
                        )
                    }.onFailure { it.printStackTrace() }.getOrNull()
                } ?: instruction
            println("ROOT: $instruction")
            println("LOADED $instructionForRoot")
            // this is the initial setup of the window, which will occur when the NavigationController
            // is first created with a root parameter, so we're going to do some slightly different
            // things to set up the window
            val binding = controller.bindingForInstruction(instructionForRoot)
            val hostedInstruction = when (binding) {
                is WebWindowNavigationBinding<*, *> -> instructionForRoot
                is ComposableNavigationBinding<*, *> -> HostComposableInWebWindow(instructionForRoot).asPresent()
                is ManagedFlowNavigationBinding<*, *> -> HostComposableInWebWindow(instructionForRoot).asPresent()
                else -> error("Cannot open ${instructionForRoot.navigationKey} in a WebWindow")
            }
            val hostedBinding = controller.bindingForInstruction(hostedInstruction)
            requireNotNull(hostedBinding) {
                "WebWindowNavigationBinding expected, but got null"
            }
            require(hostedBinding is WebWindowNavigationBinding<*, *>) {
                "WebWindowNavigationBinding expected, but got ${hostedBinding::class}"
            }
            activeWebWindow = hostedBinding.constructDestination().apply {
                this.instruction = hostedInstruction
            }
            return
        }

        /*
        There's something interesting here with hrefs and window.open calls (particularly with _self target)
        I think that we probably want to somehow relate the href to a destination,
        and use window.open to drive the JS window manager. Might need a way to iterate through
        JS registered window destinations and find one that matches a URL and use that as the root?
        Doesn't really work so well with the development server as other URLs just execute get requests

        It's also worth thinking about the multi-platform implications here. On both Android, iOS and
        the web, there's the possiblity to open a new "window" within the current context (e.g. _self)
        and a new "window" in a new context (e.g. _blank); iOS uses WindowScenes for this, Android uses
        Activities with new tasks, and the web uses new windows. Not sure if this applies to Desktop,
        but it's worth thinking about.
         */
        val hashlessWindow = window.location.href
            .removeSuffix(window.location.hash)
            .removeSuffix(window.location.search)
        val newWindow = window
            .open(hashlessWindow+"?instruction=${instruction.instructionId}", "_blank")!!

        newWindow.sessionStorage.set(
            instruction.instructionId,
            NavigationController.jsonConfiguration.encodeToString(instruction)
        )
    }

    public actual fun close(context: NavigationContext<*>, andOpen: AnyOpenInstruction?) {
        if (andOpen != null) {
            open(andOpen)
        }
        runCatching {
            window.close()
        }
    }

    internal actual fun isExplicitWindowInstruction(instruction: AnyOpenInstruction): Boolean {
        return instruction.isOpenInWindow() || controller.bindingForInstruction(instruction) is WebWindowNavigationBinding<*, *>
    }

    @Composable
    internal fun Render() {
        val window = remember {
            if (!::activeWebWindow.isInitialized) {
                error("NavigationWindowManager does not have an active WebWindow")
            }
            return@remember activeWebWindow
        }
        window.ApplyLocals(
            controller,
            content = {
                window.Render()
            },
        )
    }

    public actual companion object
}