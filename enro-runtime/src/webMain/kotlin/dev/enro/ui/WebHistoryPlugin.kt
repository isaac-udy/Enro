package dev.enro.ui
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.enro.EnroController
import dev.enro.NavigationHandle
import dev.enro.NavigationHandleConfiguration
import dev.enro.NavigationKey
import dev.enro.context.ContainerContext
import dev.enro.context.DestinationContext
import dev.enro.context.findContext
import dev.enro.context.getNavigationHandle
import dev.enro.controller.createNavigationModule
import dev.enro.path.getBackstackFromPath
import dev.enro.path.getPathFromNavigationKey
import dev.enro.platform.EnroLog
import dev.enro.plugin.NavigationPlugin
import dev.enro.requestClose
import dev.enro.ui.history.ContainerNode
import dev.enro.ui.history.HistoryTransition
import dev.enro.ui.history.applyNodeFor
import dev.enro.ui.history.awaitNodeFor
import dev.enro.ui.history.classifyTransition
import dev.enro.ui.history.closedInstanceId
import dev.enro.ui.history.createNodeFor
import dev.enro.ui.history.topDestination
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

// Browser history records the container tree under the root container (see ContainerNode).
// A push in a nested container and a change of the active nested container are history
// entries. The URL is the path of the deepest active destination that has a path.
// With `nestedContainers` false, browser history records the root container's backstack only.
//
// Each destination lifecycle callback and each browser popstate is queued on one serial
// processor, so that `historyStates` matches the session history of the browser.
internal class WebHistoryPlugin(
    private val rootContainer: ContainerContext,
    private val nestedContainers: Boolean,
) : NavigationPlugin() {

    private val scope = CoroutineScope(Dispatchers.Main)

    private class PopState(val state: String?)

    /**
     * Serial work queue. `null` requests a sync of browser history from the backstack. A
     * [PopState] requests a sync of the backstack from browser history. Each event must be
     * processed, in order: a dropped event makes [historyStates] differ from browser history.
     */
    private val events = Channel<PopState?>(capacity = Channel.UNLIMITED)

    /**
     * Non-null while [traverse] awaits the popstate caused by the plugin's own `history.go()`
     * call. The next popstate completes the deferred and is not queued as user navigation.
     */
    private var pendingTraversal: CompletableDeferred<Unit>? = null

    private val eventListener: (String?) -> Unit = { state ->
        val traversal = pendingTraversal
        if (traversal != null) {
            pendingTraversal = null
            traversal.complete(Unit)
        } else {
            events.trySend(PopState(state))
        }
    }

    private val removeEventListener: () -> Unit

    // In-memory copy of the browser history entries of this session.
    private val historyStates = mutableListOf<ContainerNode>()
    private var historyIndex = -1 // Index of the current state in historyStates

    private val processor: Job

    init {
        removeEventListener = BrowserHistory.addPopStateListener(eventListener)
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
                    // A failed sync is logged and not rethrown: a rethrow ends the processor
                    // loop, and no later history event is handled.
                    EnroLog.error("WebHistoryPlugin: history sync failed", t)
                }
            }
        }
    }

    /**
     * The plugin received no callback for a destination opened before the plugin was
     * installed. This event records the current container tree as the first history entry.
     */
    override fun onAttached(controller: EnroController) {
        events.trySend(null)
    }

    override fun onDetached(controller: EnroController) {
        removeEventListener()
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
     * Returns the `@NavigationPath` of the deepest active destination that has a path
     * binding. When no active destination has a path binding, returns the current URL: the
     * history entry is still written, and the address bar does not change.
     */
    private fun computeUrl(): String {
        val keys = mutableListOf<NavigationKey>()
        var container: ContainerContext? = rootContainer
        while (container != null) {
            keys += container.container.backstack.lastOrNull()?.key ?: break
            container = container.topDestination()?.activeChild.takeIf { nestedContainers }
        }
        return keys.asReversed()
            .firstNotNullOfOrNull { rootContainer.controller.getPathFromNavigationKey(it) }
            ?: currentUrl()
    }

    private fun currentUrl(): String = BrowserHistory.path

    /**
     * Calls `history.go(delta)` and suspends until the browser delivers the resulting
     * popstate, because `history.go` is asynchronous. The timeout covers a browser that
     * delivers no popstate, for example for a traversal at a history boundary.
     */
    private suspend fun traverse(delta: Int) {
        if (delta == 0) return
        val deferred = CompletableDeferred<Unit>()
        pendingTraversal = deferred
        BrowserHistory.go(delta)
        try {
            withTimeout(TRAVERSAL_TIMEOUT_MS) { deferred.await() }
        } catch (t: TimeoutCancellationException) {
            EnroLog.warn("WebHistoryPlugin: history traversal ($delta) produced no popstate within ${TRAVERSAL_TIMEOUT_MS}ms")
            pendingTraversal = null
        }
    }

    private fun decodeState(state: String): ContainerNode? {
        return runCatching {
            EnroController.jsonConfiguration.decodeFromString<ContainerNode>(state)
        }.onFailure { t ->
            EnroLog.warn("WebHistoryPlugin: failed to decode history state (ignoring entry): ${t.message}")
        }.getOrNull()
    }

    /**
     * Applies the recorded state of the popped history entry to the container tree, then
     * waits for the container tree to match: a restored destination registers its context
     * and containers on a later composition. When the recorded state cannot be applied (an
     * interceptor or EmptyBehavior refuses a close, or the app changed the backstack), the
     * plugin traverses back one entry and applies that entry, up to
     * [MAX_TRAVERSAL_ATTEMPTS] times.
     */
    private suspend fun syncFromPopState(event: PopState) {
        // A popstate without state (address-bar edit, cross-origin navigation) is ignored:
        // the URL alone does not identify an app state.
        val rawState = event.state ?: return
        var poppedState = decodeState(rawState)
            ?: return restoreFromUrl()
        if (requestCloseInstead(poppedState)) return

        var attempts = 0
        while (attempts < MAX_TRAVERSAL_ATTEMPTS) {
            attempts++
            val currentState = createNodeFor(rootContainer, nestedContainers)
            if (currentState == poppedState) break
            applyNodeFor(rootContainer, poppedState)
            if (awaitNodeFor(rootContainer, poppedState, nestedContainers)) break
            EnroLog.debug(
                "WebHistoryPlugin: popped state did not apply (attempt $attempts), stepping back.\n" +
                    "expected: $poppedState\nactual: ${createNodeFor(rootContainer, nestedContainers)}"
            )
            traverse(-1)
            val nextRaw = BrowserHistory.state() ?: return
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
     * Handles a browser back that only closes a destination with an `onCloseRequested`
     * callback: the plugin undoes the back with `history.go(1)`, calls `requestClose` on the
     * destination and returns true. Returns false when the destination has no callback, and
     * the caller applies the popped entry.
     */
    private suspend fun requestCloseInstead(target: ContainerNode): Boolean {
        val closedId = closedInstanceId(createNodeFor(rootContainer, nestedContainers), target) ?: return false
        val destination = rootContainer.findContext {
            it is DestinationContext<*> && it.instance.id == closedId
        } as? DestinationContext<*> ?: return false
        val callbacks = destination.instance.metadata.get(NavigationHandleConfiguration.OnCloseCallbacks)
        if (callbacks.isEmpty()) return false
        traverse(1)
        destination.getNavigationHandle().requestClose()
        return true
    }

    /**
     * Fallback for a history entry whose recorded state cannot be decoded, for example an
     * entry written by an older build of the app. Resolves the URL of the entry through the
     * path bindings of the controller, applies the resulting backstack, and replaces the
     * state of the entry with the serialized container tree.
     */
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
        val currentState = createNodeFor(rootContainer, nestedContainers)
        BrowserHistory.replaceState(serializeForHistory(currentState), computeUrl())
        val index = historyStates.indexOfFirst { it == currentState }
        if (index != -1) {
            historyIndex = index
        } else {
            historyStates.add(currentState)
            historyIndex = historyStates.lastIndex
        }
    }

    /**
     * Serializes [state] for `history.state` and checks that the result decodes (see
     * SerializerRepository.jsonConfiguration for a known kotlinx.serialization failure).
     *
     * A result that does not decode throws: a history entry with that state could not be
     * restored on browser back. The error lists the in-memory metadata of each instance,
     * because the serialized form does not show the metadata value types.
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
     * Writes the current container tree to browser history.
     */
    private suspend fun syncFromBackstack() {
        val currentState = createNodeFor(rootContainer, nestedContainers)
        val serializedCurrentState = serializeForHistory(currentState)

        val windowState = BrowserHistory.state()?.let(::decodeState)

        val isInit = historyStates.isEmpty() && historyIndex == -1
        val isNoOp = windowState != null && windowState == currentState
        val closeIndex = historyStates.indexOfLast { it == currentState }

        when {
            isInit -> {
                historyStates.add(currentState)
                historyIndex = 0
                BrowserHistory.replaceState(serializedCurrentState, computeUrl())
            }

            isNoOp -> {
                if (closeIndex >= 0) {
                    historyIndex = closeIndex
                    historyStates[historyIndex] = currentState
                }
                BrowserHistory.replaceState(serializedCurrentState, computeUrl())
            }

            closeIndex >= 0 -> {
                // The current state is an existing history entry, so a destination was
                // closed. The plugin traverses to the existing entry.
                val previousIndex = historyIndex
                historyIndex = closeIndex
                historyStates[historyIndex] = currentState
                if (closeIndex == 0) {
                    traverse(closeIndex - previousIndex)
                    BrowserHistory.replaceState(serializedCurrentState, computeUrl())
                } else {
                    // Traverses to the entry before the target, then pushes the target. The push
                    // removes the browser's forward entries, so browser forward cannot reopen
                    // a closed destination. Index 0 has no earlier entry to traverse to.
                    traverse(closeIndex - previousIndex - 1)
                    BrowserHistory.pushState(serializedCurrentState, computeUrl())
                    // historyStates must drop the forward entries that the push removed.
                    historyStates.subList(historyIndex + 1, historyStates.size).clear()
                }
            }

            else -> {
                // The current state is not in historyStates. classifyTransition decides whether
                // the state is pushed (forward navigation) or replaces the current entry (for
                // example a root reset, or the containers of a destination registering).
                val previous = historyStates.getOrNull(historyIndex)
                val transition = previous?.let { classifyTransition(it, currentState) }
                historyStates.subList(historyIndex + 1, historyStates.size).clear()
                if (transition == null || transition == HistoryTransition.Push) {
                    historyStates.add(currentState)
                    historyIndex = historyStates.lastIndex
                    BrowserHistory.pushState(serializedCurrentState, computeUrl())
                } else {
                    historyStates[historyIndex] = currentState
                    BrowserHistory.replaceState(serializedCurrentState, computeUrl())
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
 * Experimental. Synchronises [container] with browser history, so that browser back and
 * forward change the backstack of [container].
 *
 * @param nestedContainerHistory when true, a push in a container nested under a destination
 *  of [container], and a change of the active nested container, each add a browser history
 *  entry, and the URL is the path of the deepest active destination. When false, browser
 *  history records the backstack of [container] only.
 */
@Composable
public fun InstallWebHistoryPlugin(
    container: NavigationContainerState,
    nestedContainerHistory: Boolean = true,
) {
    LaunchedEffect(Unit) {
        container.context.controller.addModule(
            createNavigationModule {
                plugin(WebHistoryPlugin(
                    rootContainer = container.context,
                    nestedContainers = nestedContainerHistory,
                ))
            }
        )
    }
}
