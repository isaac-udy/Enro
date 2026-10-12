package dev.enro.ui

import kotlinx.browser.window
import org.w3c.dom.PopStateEvent
import org.w3c.dom.events.Event

internal actual object BrowserHistory {
    actual val path: String
        get() = window.location.pathname + window.location.search

    actual fun state(): String? = window.history.state as? String

    actual fun pushState(state: String, url: String) {
        window.history.pushState(state, "", url)
    }

    actual fun replaceState(state: String, url: String) {
        window.history.replaceState(state, "", url)
    }

    actual fun go(delta: Int) {
        window.history.go(delta)
    }

    actual fun addPopStateListener(listener: (state: String?) -> Unit): () -> Unit {
        val callback: (Event) -> Unit = { event ->
            if (event is PopStateEvent) listener(event.state as? String)
        }
        window.addEventListener("popstate", callback)
        return { window.removeEventListener("popstate", callback) }
    }
}
