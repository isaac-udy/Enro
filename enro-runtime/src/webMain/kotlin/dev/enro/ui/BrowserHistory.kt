package dev.enro.ui

/**
 * The `window.history` / `window.location` calls the web history support needs. js and wasmJs
 * declare the DOM with different interop types (`Any?` vs `JsAny?`), so history state crosses
 * this boundary as the serialized JSON string the plugin writes.
 */
internal expect object BrowserHistory {
    val path: String
    fun state(): String?
    fun pushState(state: String, url: String)
    fun replaceState(state: String, url: String)
    fun go(delta: Int)

    /** Registers [listener] for `popstate`, passing the event's state; returns the unregister call. */
    fun addPopStateListener(listener: (state: String?) -> Unit): () -> Unit
}
