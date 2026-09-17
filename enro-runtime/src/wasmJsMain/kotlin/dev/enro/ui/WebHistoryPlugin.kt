package dev.enro.ui
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.enro.EnroController
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.context.ContainerContext
import dev.enro.controller.createNavigationModule
import dev.enro.path.getBackstackFromPath
import dev.enro.path.getPathFromNavigationKey
import dev.enro.platform.EnroLog
import dev.enro.plugin.NavigationPlugin
import dev.enro.ui.history.ContainerNode
import dev.enro.ui.history.HistoryTransition
import dev.enro.ui.history.applyNodeFor
import dev.enro.ui.history.awaitNodeFor
import dev.enro.ui.history.classifyTransition
import dev.enro.ui.history.createNodeFor
import dev.enro.ui.history.topDestination
import kotlinx.browser.window
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.w3c.dom.PopStateEvent
import org.w3c.dom.Window
import org.w3c.dom.events.Event

// Browser history mirrors the whole container tree under the root: the root container's
// backstack, and beneath the destination on top of it the containers that destination hosts,
// recursively (see ContainerNode). A push inside a nested container — a screen opened within
// a tab, a detail pane — is a history entry like a push on the root, and so is a change of
// which nested container is active, which is a tab switch. The URL is the path of the deepest
// active destination that has one.
//
// Synchronisation model: every input (destination lifecycle callback or browser
// popstate) is enqueued onto a single serial processor, so updates are never dropped
// and the in-memory mirror of browser history can't silently diverge from the real
// session history. History traversals the plugin itself initiates (`history.go`)
// are awaited via their popstate echo, which is consumed before it can be mistaken
// for a user-initiated back/forward.
@ExperimentalEnroApi
internal class WebHistoryPlugin(
    private val window: Window,
    private val rootContainer: ContainerContext,
) : NavigationPlugin() {

    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * Serial work queue. `null` means "the backstack changed, re-sync browser
     * history"; a [PopStateEvent] means "the browser navigated, re-sync the
     * backstack". Processing strictly in order is what keeps [historyStates]
     * truthful — the previous implementation dropped events that arrived while
     * a sync was in flight, which desynced the mirror and made a single
     * browser back traverse multiple app screens.
     */
    private val events = Channel<PopStateEvent?>(capacity = Channel.UNLIMITED)

    /**
     * Set while the plugin is awaiting the popstate echo of its own
     * `history.go()` call — see [traverse]. The next popstate completes it and
     * is consumed instead of being enqueued as user navigation.
     */
    private var pendingTraversal: CompletableDeferred<Unit>? = null

    private val eventListener: (Event) -> Unit = { event ->
        if (event is PopStateEvent) {
            val traversal = pendingTraversal
            if (traversal != null) {
                pendingTraversal = null
                traversal.complete(Unit)
            } else {
                events.trySend(event)
            }
        }
    }

    // In-memory representation of the browser history for this session
    private val historyStates = mutableListOf<ContainerNode>()
    private var historyIndex = -1 // Index of the current state in historyStates

    private val processor: Job

    init {
        window.addEventListener("popstate", eventListener)
        processor = scope.launch {
            for (event in events) {
                try {
                    when (event) {
                        null -> syncFromBackstack()
                        else -> syncFromPopState(event)
                    }
                } catch (c: CancellationException) {
                    throw c
                } catch (t: Throwable) {
                    // One failed sync must not kill history handling for the
                    // rest of the session — without this, a single throwing
                    // serializer/interceptor/path computation would end the
                    // processor loop and browser back would go silent while
                    // the URL keeps changing natively.
                    EnroLog.error("WebHistoryPlugin: history sync failed", t)
                }
            }
        }
    }

    /**
     * The destinations already on screen opened before the plugin was installed, so their
     * callbacks never reached it; this records them as the first entry.
     */
    override fun onAttached(controller: EnroController) {
        events.trySend(null)
    }

    override fun onDetached(controller: EnroController) {
        window.removeEventListener("popstate", eventListener)
        processor.cancel()
    }

    override fun onOpened(navigationHandle: NavigationHandle<*>) {
        events.trySend(null)
    }

    override fun onActive(navigationHandle: NavigationHandle<*>) {
        events.trySend(null)
    }

    override fun onClosed(navigationHandle: NavigationHandle<*>) {
        events.trySend(null)
    }

    /**
     * Computes the URL to write to `window.history`: the `@NavigationPath` of the
     * deepest active destination that has one, walking from the root container
     * through each destination's active container. When no destination on that
     * walk has a path binding, the existing address-bar URL is preserved —
     * `pushState` still fires (so back/forward works through `history.state`),
     * but the visible URL doesn't change. That keeps bookmarkable URLs honest:
     * only destinations that opt in to a path produce a path.
     */
    @OptIn(ExperimentalEnroApi::class)
    private fun computeUrl(): String {
        val keys = mutableListOf<NavigationKey>()
        var container: ContainerContext? = rootContainer
        while (container != null) {
            keys += container.container.backstack.lastOrNull()?.key ?: break
            container = container.topDestination()?.activeChild
        }
        return keys.asReversed()
            .firstNotNullOfOrNull { rootContainer.controller.getPathFromNavigationKey(it) }
            ?: currentUrl()
    }

    private fun currentUrl(): String {
        return window.location.pathname + window.location.search
    }

    /**
     * Calls `history.go(delta)` and suspends until the browser delivers the
     * resulting popstate, consuming that echo. `history.go` is asynchronous —
     * the previous implementation `delay(1)`-ed and hoped, which raced the
     * traversal (corrupting the history position) and let the echo arrive
     * after suppression was lifted, where it was processed as a second
     * user back. The timeout is a safety valve for browsers that elide the
     * event (e.g. a no-op traversal at a history boundary).
     */
    private suspend fun traverse(delta: Int) {
        if (delta == 0) return
        val deferred = CompletableDeferred<Unit>()
        pendingTraversal = deferred
        window.history.go(delta)
        try {
            withTimeout(TRAVERSAL_TIMEOUT_MS) { deferred.await() }
        } catch (t: TimeoutCancellationException) {
            EnroLog.warn("WebHistoryPlugin: history traversal ($delta) produced no popstate within ${TRAVERSAL_TIMEOUT_MS}ms")
            pendingTraversal = null
        }
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    private fun decodeState(state: JsAny): ContainerNode? {
        return runCatching {
            EnroController.jsonConfiguration.decodeFromString<ContainerNode>(state.toString())
        }.onFailure { t ->
            EnroLog.warn("WebHistoryPlugin: failed to decode history state (ignoring entry): ${t.message}")
        }.getOrNull()
    }

    /**
     * The browser navigated (user back/forward): drive the container tree to
     * match the entry's recorded state. Applying takes a frame or two to show
     * in the tree — a restored destination registers its context and its
     * containers on the next composition — so the tree is awaited before it is
     * judged. When a recorded state can't be applied (an interceptor or
     * EmptyBehavior refuses the close, or the app rewrote the backstack
     * concurrently), step past it — bounded, rather than blind-firing
     * `history.back()` and re-entering through the listener.
     */
    @OptIn(ExperimentalWasmJsInterop::class)
    private suspend fun syncFromPopState(event: PopStateEvent) {
        // popstate without a state payload (manual address-bar edit, cross-origin
        // nav). We can't safely restore a sensible app state from URL alone —
        // no-op and let the user reload if they want the URL to take effect.
        val rawState = event.state ?: return
        var poppedState = decodeState(rawState)
            ?: return restoreFromUrl()

        var attempts = 0
        while (attempts < MAX_TRAVERSAL_ATTEMPTS) {
            attempts++
            val currentState = createNodeFor(rootContainer)
            if (currentState == poppedState) break
            applyNodeFor(rootContainer, poppedState)
            if (awaitNodeFor(rootContainer, poppedState)) break
            // The recorded state didn't take — step one entry further back and
            // try that one instead.
            EnroLog.debug(
                "WebHistoryPlugin: popped state did not apply (attempt $attempts), stepping back.\n" +
                    "expected: $poppedState\nactual: ${createNodeFor(rootContainer)}"
            )
            traverse(-1)
            val nextRaw = window.history.state ?: return
            poppedState = decodeState(nextRaw)
                ?: return restoreFromUrl()
        }

        val poppedIndex = historyStates.indexOfFirst { it == poppedState }
        if (poppedIndex != -1) {
            historyIndex = poppedIndex
        } else {
            historyStates.add(poppedState)
            historyIndex = historyStates.lastIndex
        }
    }

    /**
     * Fallback for a history entry whose recorded state can't be decoded —
     * typically an entry written by an older build of the app whose
     * serialization no longer matches (stale tab history survives deploys),
     * or a metadata value that doesn't round-trip. Resolves the entry's URL
     * through the controller's path bindings instead — degraded (single
     * entry, same semantics as a cold-load deep link) but functional — and
     * self-heals the entry by overwriting its unreadable state with the
     * freshly serialized equivalent so the next visit decodes normally.
     */
    @OptIn(ExperimentalWasmJsInterop::class)
    private suspend fun restoreFromUrl() {
        val fallback = rootContainer.controller.getBackstackFromPath(currentUrl())
        if (fallback == null) {
            EnroLog.warn(
                "WebHistoryPlugin: history entry state was unreadable and its URL " +
                    "('${currentUrl()}') has no path binding — leaving app state unchanged"
            )
            return
        }
        applyNodeFor(rootContainer, ContainerNode(
            containerKey = rootContainer.container.key,
            backstack = fallback,
        ))
        val currentState = createNodeFor(rootContainer)
        val serializedCurrentState = serializeForHistory(currentState).toJsString()
        window.history.replaceState(serializedCurrentState, "", computeUrl())
        val index = historyStates.indexOfFirst { it == currentState }
        if (index != -1) {
            historyIndex = index
        } else {
            historyStates.add(currentState)
            historyIndex = historyStates.lastIndex
        }
    }

    /**
     * Serializes [state] for storage in `history.state`, verifying the result
     * actually decodes. Encode-and-decode-back is cheap insurance against
     * serialization shapes kotlinx mishandles (see the discriminator-mode
     * note on SerializerRepository.jsonConfiguration for the class of bug
     * this guards against).
     *
     * Verification failure is a hard error — writing a state that can't
     * restore would silently break browser back for the entry, and degrading
     * (e.g. stripping metadata) would silently lose data such as
     * result-channel wiring, which is worse than failing loudly. The error
     * includes the live in-memory metadata: the serialized form mangles the
     * offending entry, but the in-memory map still has the real keys and
     * value types.
     */
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    private fun serializeForHistory(state: ContainerNode): String {
        val serialized = EnroController.jsonConfiguration.encodeToString(state)
        val verification = runCatching {
            EnroController.jsonConfiguration.decodeFromString<ContainerNode>(serialized)
        }
        if (verification.isSuccess) return serialized

        val metadataDescription = state.backstack.joinToString { instance ->
            val entries = instance.metadata.map.entries.joinToString { (key, value) ->
                "$key=${value::class.simpleName}"
            }
            "${instance.key::class.simpleName}[$entries]"
        }
        error(
            "WebHistoryPlugin: serialized history state failed round-trip verification " +
                "(${verification.exceptionOrNull()?.message}). This entry would not restore on " +
                "browser back, so it has NOT been written to history. In-memory metadata by " +
                "instance: $metadataDescription. State: $serialized"
        )
    }

    /**
     * The backstack changed (open/active/close): mirror it into browser history.
     */
    @OptIn(ExperimentalWasmJsInterop::class)
    private suspend fun syncFromBackstack() {
        val currentState = createNodeFor(rootContainer)
        val serializedCurrentState = serializeForHistory(currentState).toJsString()

        val windowState = window.history.state?.let(::decodeState)

        val isInit = historyStates.isEmpty() && historyIndex == -1
        val isNoOp = windowState != null && windowState == currentState
        val closeIndex = historyStates.indexOfLast { it == currentState }

        when {
            isInit -> {
                historyStates.add(currentState)
                historyIndex = 0
                window.history.replaceState(serializedCurrentState, "", computeUrl())
            }

            isNoOp -> {
                if (closeIndex >= 0) {
                    historyIndex = closeIndex
                    historyStates[historyIndex] = currentState
                }
                window.history.replaceState(serializedCurrentState, "", computeUrl())
            }

            closeIndex >= 0 -> {
                // The current state exists earlier in the history: this is a close,
                // pop back to that entry.
                val previousIndex = historyIndex
                historyIndex = closeIndex
                historyStates[historyIndex] = currentState
                if (closeIndex == 0) {
                    traverse(closeIndex - previousIndex)
                    window.history.replaceState(serializedCurrentState, "", computeUrl())
                } else {
                    // Land one short of the target and push it fresh: pruning the
                    // browser's forward entries so forward can't resurrect screens
                    // the app has closed. (Not possible at index 0 — there's no
                    // entry before it to land on.)
                    traverse(closeIndex - previousIndex - 1)
                    window.history.pushState(serializedCurrentState, "", computeUrl())
                    // The push destroyed the browser's forward entries — drop them
                    // from the mirror too.
                    historyStates.subList(historyIndex + 1, historyStates.size).clear()
                }
            }

            else -> {
                // A state we haven't seen: forward navigation pushes an entry,
                // anything else — a root reset such as loading → home, a
                // truncate-and-open section switch, or a destination's containers
                // still filling in — replaces the current one. See
                // classifyTransition for the line between them.
                val previous = historyStates.getOrNull(historyIndex)
                val transition = previous?.let { classifyTransition(it, currentState) }
                historyStates.subList(historyIndex + 1, historyStates.size).clear()
                if (transition == null || transition == HistoryTransition.Push) {
                    historyStates.add(currentState)
                    historyIndex = historyStates.lastIndex
                    window.history.pushState(serializedCurrentState, "", computeUrl())
                } else {
                    historyStates[historyIndex] = currentState
                    window.history.replaceState(serializedCurrentState, "", computeUrl())
                }
            }
        }
    }

    private companion object {
        const val TRAVERSAL_TIMEOUT_MS = 250L
        const val MAX_TRAVERSAL_ATTEMPTS = 10
    }
}

/**
 * Experimental browser-based back handling
 */
@ExperimentalEnroApi
@Composable
public fun InstallWebHistoryPlugin(
    container: NavigationContainerState,
) {
    LaunchedEffect(Unit) {
        container.context.controller.addModule(
            createNavigationModule {
                plugin(WebHistoryPlugin(
                    window = window,
                    rootContainer = container.context,
                ))
            }
        )
    }
}
