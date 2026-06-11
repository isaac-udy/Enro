package dev.enro.ui
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.enro.EnroController
import dev.enro.NavigationBackstack
import dev.enro.NavigationContainer
import dev.enro.NavigationHandle
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.context.ContainerContext
import dev.enro.controller.createNavigationModule
import dev.enro.emptyBackstack
import dev.enro.path.getPathFromNavigationKey
import dev.enro.platform.EnroLog
import dev.enro.plugin.NavigationPlugin
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
import kotlinx.coroutines.yield
import kotlinx.serialization.Serializable
import org.w3c.dom.PopStateEvent
import org.w3c.dom.Window
import org.w3c.dom.events.Event

// Root-container-only history: only the root container's backstack participates in
// browser history. Inner-container navigation (modals, tabs, list-detail panes, etc.)
// is session-local and not reflected in the URL or back/forward history. This is the
// "Twitter/X / Reddit" model — pages get URLs, page-internal state does not.
//
// Nested URL routing is a known future direction; see docs/ghpages/docs/platform/web.md
// for the model we ship in beta.
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

    override fun onAttached(controller: EnroController) {}

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
     * Computes the URL to write to `window.history`. Uses the `@NavigationPath`
     * registered against the root container's active destination. When that key
     * has no path binding, the existing address-bar URL is preserved — `pushState`
     * still fires (so back/forward works through `history.state`), but the
     * visible URL doesn't change. That keeps bookmarkable URLs honest: only
     * destinations that opt in to a path produce a path.
     *
     * Inner-container navigation is also invisible to the URL — see the web
     * platform docs for the model.
     */
    @OptIn(ExperimentalEnroApi::class)
    private fun computeUrl(): String {
        val rootKey = rootContainer.activeChild?.key ?: return currentUrl()
        return rootContainer.controller.getPathFromNavigationKey(rootKey) ?: currentUrl()
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
     * The browser navigated (user back/forward): drive the backstack to match
     * the entry's recorded state. When a recorded state can't be applied (an
     * interceptor or EmptyBehavior refuses the close, or the app rewrote the
     * backstack concurrently), step past it — bounded, rather than blind-firing
     * `history.back()` and re-entering through the listener.
     */
    @OptIn(ExperimentalWasmJsInterop::class)
    private suspend fun syncFromPopState(event: PopStateEvent) {
        // popstate without a state payload (manual address-bar edit, cross-origin
        // nav). Under root-only routing we can't safely restore a sensible app
        // state from URL alone — no-op and let the user reload if they want the
        // URL to take effect.
        var poppedState = event.state?.let(::decodeState) ?: return

        var attempts = 0
        while (attempts < MAX_TRAVERSAL_ATTEMPTS) {
            attempts++
            val currentState = createNodeFor(rootContainer)
            if (currentState == poppedState) break
            applyNodeFor(rootContainer, poppedState)
            if (createNodeFor(rootContainer) == poppedState) break
            // The recorded state didn't take — step one entry further back and
            // try that one instead.
            traverse(-1)
            poppedState = window.history.state?.let(::decodeState) ?: return
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
     * The backstack changed (open/active/close): mirror it into browser history.
     */
    @OptIn(ExperimentalWasmJsInterop::class)
    private suspend fun syncFromBackstack() {
        val currentState = createNodeFor(rootContainer)
        val serializedCurrentState = EnroController.jsonConfiguration
            .encodeToString(currentState)
            .toJsString()

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
                // A state we haven't seen. Forward navigation (push) only when the
                // previous state is a prefix of the new one — i.e. entries were
                // added on top of what was already there. Anything else (a root
                // reset such as loading → home, or a truncate-and-open section
                // switch) REPLACES the current entry: the state it overwrites is
                // no longer reachable in the app and must not survive as a browser
                // back target.
                val previous = historyStates.getOrNull(historyIndex)
                val isPush = previous == null || isSubset(old = currentState, new = previous)
                historyStates.subList(historyIndex + 1, historyStates.size).clear()
                if (isPush) {
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


@Serializable
internal data class ContainerNode(
    val containerKey: NavigationContainer.Key,
    val backstack: NavigationBackstack,
    val children: List<ContainerNode>,
) {
    override fun toString(): String {
        val content = "backstack = [${backstack.joinToString { it.navigationKey.toString() }}],\n" +
                "children = [${children.joinToString { it.toString() }}],\n"
        return buildString {
            appendLine("ContainerNode(")
            content.lines().forEach {
                appendLine(it.prependIndent("    "))
            }
            append(")")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other::class != this::class) return false

        other as ContainerNode

        if (containerKey != other.containerKey) return false
        if (backstack.map { it.id } != other.backstack.map { it.id }) return false

        val filteredChildren =
            children.filter { it.backstack.isNotEmpty() }.sortedBy { it.containerKey.name }
        val otherFilteredChildren =
            other.children.filter { it.backstack.isNotEmpty() }.sortedBy { it.containerKey.name }
        if (filteredChildren.size != otherFilteredChildren.size) return false
        filteredChildren.forEachIndexed { index, child ->
            if (child != otherFilteredChildren[index]) return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = containerKey.hashCode()
        result = 31 * result + backstack.map { it.id }.hashCode()
        result = 31 * result + children.filter { it.backstack.isNotEmpty() }
            .sortedBy { it.containerKey.name }.hashCode()
        return result
    }
}

internal fun createNodeFor(
    container: ContainerContext,
): ContainerNode {
    return ContainerNode(
        containerKey = container.container.key,
        backstack = container.container.backstack,
        children = emptyList(),
    )
}

internal suspend fun applyNodeFor(
    container: ContainerContext,
    node: ContainerNode,
) {
    if (container.container.backstack != node.backstack) {
        container.container.updateBackstack(container) { node.backstack }
    }
    // If the backstack is empty, we don't need to do anything else,
    // so can return early, otherwise we're going to wait for the
    // child context to be set before we continue
    if (node.children.isEmpty()) return
    val childContext = withTimeout(64) {
        while (container.activeChild?.instance?.id != node.backstack.lastOrNull()?.id) {
            yield()
        }
        container.activeChild
    }
    if (childContext == null) {
        EnroLog.warn("WebHistoryPlugin: failed to restore child container while applying popped state")
        return
    }
    val containers = childContext.children
        .associateBy { it.container.key }
        .toMutableMap()

    node.children.forEach { childNode ->
        val child = containers[childNode.containerKey]
        if (child != null) {
            applyNodeFor(child, childNode)
        }
        containers.remove(childNode.containerKey)
    }
    containers.forEach { (_, child) ->
        child.container.updateBackstack(child) { emptyBackstack() }
    }
}

/**
 * True when [new] is a prefix-subset of [old] — i.e. [new] contains no entries
 * that aren't already in [old], in the same order from the root. Used to
 * distinguish a genuine forward push (the previous state is a subset of the
 * next) from a replacement (entries were swapped out in a single transition).
 */
internal fun isSubset(old: ContainerNode, new: ContainerNode): Boolean {
    fun isNodeSubset(oldNode: ContainerNode, newNode: ContainerNode): Boolean {
        if (oldNode.containerKey != newNode.containerKey) {
            return false
        }

        val oldInstructionIds = oldNode.backstack.map { it.id }
        val newInstructionIds = newNode.backstack.map { it.id }

        // Check if the new backstack is a prefix of the old backstack
        if (!newInstructionIds.zip(oldInstructionIds)
                .all { it.first == it.second } || newInstructionIds.size > oldInstructionIds.size
        ) {
            return false
        }

        val oldChildrenSorted = oldNode.children.sortedBy { it.containerKey.name }
        val newChildrenSorted = newNode.children.sortedBy { it.containerKey.name }

        if (newChildrenSorted.size > oldChildrenSorted.size) return false

        for (i in newChildrenSorted.indices) {
            val matchingOldChild = oldChildrenSorted.getOrNull(i)
            if (matchingOldChild == null || !isNodeSubset(matchingOldChild, newChildrenSorted[i])) {
                return false
            }
        }
        return true
    }

    // We need to find a path in the old tree that matches the structure of the new tree
    fun findMatchInOld(oldRoot: ContainerNode, newRoot: ContainerNode): Boolean {
        if (oldRoot.containerKey == newRoot.containerKey && isNodeSubset(oldRoot, newRoot)) {
            if (newRoot.children.isEmpty()) return true
            return newRoot.children.all { newChild ->
                oldRoot.children.any { oldChild -> findMatchInOld(oldChild, newChild) }
            }
        }
        return oldRoot.children.any { findMatchInOld(it, newRoot) }
    }

    return findMatchInOld(old, new)
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
