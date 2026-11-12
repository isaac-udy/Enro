package dev.enro.ui

/**
 * The `window.history` and `window.location` calls used by WebHistoryPlugin. js and wasmJs
 * declare the DOM with different interop types (`Any?` and `JsAny?`), so history state is
 * passed as the serialized JSON string that WebHistoryPlugin writes.
 */
internal expect object BrowserHistory {
    val path: String
    fun state(): String?
    fun pushState(state: String, url: String)
    fun replaceState(state: String, url: String)
    fun go(delta: Int)

    /** Registers [listener] for `popstate` with the state of the event. Returns a function that unregisters [listener]. */
    fun addPopStateListener(listener: (state: String?) -> Unit): () -> Unit
}
