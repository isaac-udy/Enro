
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationHandle
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.emptyBackstack
import dev.enro.core.container.toBackstack
import dev.enro.core.controller.NavigationController
import dev.enro.core.plugins.EnroPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import kotlinx.serialization.Serializable
import org.w3c.dom.PopStateEvent
import org.w3c.dom.Window
import org.w3c.dom.events.Event

class WebHistoryPlugin(
    private val window: Window,
) : EnroPlugin() {

    internal var rootContainer: NavigationContainer? = null
        set(value) {
            field = value
            if (value != null) {
                updateHistoryState()
            }
        }

    private var eventListenerEnabled = true
    private val eventListener: (Event) -> Unit = {
        if (eventListenerEnabled && it is PopStateEvent) {
            updateHistoryState(it)
//            rootContainer?.context?.leafContext()?.getNavigationHandle()?.requestClose()
        }
    }

    // In-memory representation of the browser history for this session
    private val historyStates = mutableListOf<ContainerNode>()
    private var historyIndex = -1 // Index of the current state in historyStates

    init {
        window.addEventListener("popstate", eventListener)
    }

    override fun onAttached(navigationController: NavigationController) {}

    override fun onDetached(navigationController: NavigationController) {}

    override fun onOpened(navigationHandle: NavigationHandle) {
        updateHistoryState()
    }

    override fun onActive(navigationHandle: NavigationHandle) {
        updateHistoryState()
    }

    override fun onClosed(navigationHandle: NavigationHandle) {
        updateHistoryState()
    }
    var job: Job? = null
    private fun updateHistoryState(
        event: PopStateEvent? = null,
    ) {
        val container = rootContainer ?: return
        eventListenerEnabled = false
        if (job != null) {
            return
        }
        println("START")
        job = CoroutineScope(Dispatchers.Main).launch {
            val currentState = createNodeFor(container)
            val serializedCurrentState = NavigationController.jsonConfiguration
                .encodeToString(currentState)
                .toJsString()

            val windowState = window.history.state?.let {
                NavigationController.jsonConfiguration
                    .decodeFromString<ContainerNode>(it.toString())
            }

            if (event != null && event.state != null) {
                val poppedState = NavigationController.jsonConfiguration
                    .decodeFromString<ContainerNode>(event.state.toString())
                if (currentState != poppedState) {
                    println("History browserpop")
                    applyNodeFor(container, poppedState)
                    val updatedState = createNodeFor(container)
                    if (updatedState != poppedState) {
                        window.history.back()
                        return@launch
                    }
                    val poppedIndex = historyStates.indexOfFirst { it == poppedState }
                    if (poppedIndex != -1) {
                        historyIndex = poppedIndex
                    } else {
                        historyStates.add(poppedState)
                        historyIndex = historyStates.lastIndex
                    }
                } else {
                    println("History browserpop noop")
                }
            } else { // Not a popstate event (opened, active, closed, init)
                val isInit = historyStates.isEmpty() && historyIndex == -1
                val isNoOp = windowState != null && windowState == currentState

                val closeIndex = historyStates.indexOfLast { it == currentState }
                println("currentState = $currentState")
                println("windowState = $windowState")
                println("historyIndex = $historyIndex")
                historyStates.forEachIndexed { index, it ->
                    println("historyStates[${index}] = $it")
                }
                println("--")
                val isClose = closeIndex >= 0

                when {
                    isInit -> {
                        println("History init")
                        historyStates.add(currentState)
                        historyIndex = 0
                        window.history.replaceState(
                            serializedCurrentState,
                            "example",
                            "#${historyIndex}"
                        )
                    }

                    isNoOp -> {
                        val windowIndex = historyStates.indexOfLast { it == currentState }
                        historyIndex = windowIndex
                        historyStates[historyIndex] = currentState
                        window.history.replaceState(
                            serializedCurrentState,
                            "example",
                            "#${historyIndex}"
                        )
                    }

                    isClose -> {
                        println("History close")
                        // when the target state is a close, we need to pop back to that element in the history
                        val previousIndex = historyIndex
                        historyIndex = closeIndex
                        historyStates[historyIndex] = currentState
                        val goDelta = closeIndex - previousIndex
                        if (closeIndex == 0) {
                            println("going back delta replace: ${goDelta}")
                            window.history.go(goDelta)
                            window.history.replaceState(
                                serializedCurrentState,
                                "example",
                                "#${historyIndex}"
                            )
                        } else {
                            println("going back delta push: ${goDelta}")
                            window.history.go(goDelta - 1)
                            delay(1)
                            window.history.pushState(
                                serializedCurrentState,
                                "example",
                                "#${historyIndex}"
                            )
                        }
                    }
                    else -> {
                        val currentIndex = historyStates.indexOfLast { it == currentState }
                        if (currentIndex < 0) {
                            println("History push new")
                            historyStates.subList(historyIndex + 1, historyStates.size).clear()
                            historyStates.add(currentState)
                            historyIndex = historyStates.lastIndex
                            window.history.pushState(
                                serializedCurrentState,
                                "example",
                                "#${historyIndex}"
                            )
                        } else {
                            val previousIndex = historyIndex
                            historyIndex = currentIndex
                            historyStates[historyIndex] = currentState
                            println("History push existing")
                            window.history.go(previousIndex - currentIndex)
                            delay(1)
                            window.history.pushState(
                                serializedCurrentState,
                                "example",
                                "#${historyIndex}"
                            )
                        }
                    }
                }
            }
            delay(1)
        }.apply {
            invokeOnCompletion {
                println("END")
                job = null
                eventListenerEnabled = true
            }
        }
    }
}


@Serializable
data class ContainerNode(
    val containerKey: NavigationContainerKey,
    val backstack: List<AnyOpenInstruction>,
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
        if (backstack.map { it.instructionId } != other.backstack.map { it.instructionId }) return false

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
        result = 31 * result + backstack.map { it.instructionId }.hashCode()
        result = 31 * result + children.filter { it.backstack.isNotEmpty() }
            .sortedBy { it.containerKey.name }.hashCode()
        return result
    }
}

fun createNodeFor(
    container: NavigationContainer,
): ContainerNode {
    return ContainerNode(
        containerKey = container.key,
        backstack = container.backstack.toList(),
        children = container.childContext?.containerManager?.containers.orEmpty()
            .map { createNodeFor(it) }
            .sortedBy { it.containerKey.name }
    )
}

suspend fun applyNodeFor(
    container: NavigationContainer,
    node: ContainerNode,
) {
    if (container.backstack != node.backstack.toBackstack()) {
        container.setBackstack(node.backstack.toBackstack())
    }
    // If the backstack is empty, we don't need to do anything else,
    // so can return early, otherwise we're going to wait for the
    // child context to be set before we continue
    if (node.children.isEmpty()) return
    val childContext = withTimeout(64) {
        while (container.childContext?.instruction?.instructionId != node.backstack.lastOrNull()?.instructionId) {
            yield()
        }
        container.childContext
    }
    if (childContext == null) {
        println("Failed to restore")
        return
    }
    val containers = childContext.containerManager.containers
        .associateBy { it.key }
        .toMutableMap()

    node.children.forEach { childNode ->
        val child = containers[childNode.containerKey]
        if (child != null) {
            applyNodeFor(child, childNode)
        }
        containers.remove(childNode.containerKey)
    }
    containers.forEach { (_, child) ->
        child.setBackstack(emptyBackstack())
    }
}

fun isNewState(old: ContainerNode, new: ContainerNode): Boolean {
    val oldInstructions = collectInstructionIds(old).toSet()
    val newInstructions = collectInstructionIds(new).toSet()

    // If the new tree has instructions not in the old tree, it's a new state
    if (newInstructions.any { it !in oldInstructions }) {
        return true
    }

    // Check if the new tree is a subset of the old tree
    return !isSubset(old, new)
}

fun collectInstructionIds(node: ContainerNode): List<String> {
    val instructions = node.backstack.map { it.instructionId }
    return instructions + node.children.flatMap { collectInstructionIds(it) }
}

fun isSubset(old: ContainerNode, new: ContainerNode): Boolean {
    fun isNodeSubset(oldNode: ContainerNode, newNode: ContainerNode): Boolean {
        if (oldNode.containerKey != newNode.containerKey) {
            return false
        }

        val oldInstructionIds = oldNode.backstack.map { it.instructionId }
        val newInstructionIds = newNode.backstack.map { it.instructionId }

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
