package dev.enro.core.container

import dev.enro.core.*

fun createEmptyBackStack() = NavigationContainerBackstack(
    lastInstruction = NavigationInstruction.Close,
    backstack = emptyList(),
    exiting = null,
    exitingIndex = -1,
    isDirectUpdate = true
)

fun createRootBackStack(rootInstruction: AnyOpenInstruction) = NavigationContainerBackstack(
    lastInstruction = NavigationInstruction.Close,
    backstack = listOf(rootInstruction),
    exiting = null,
    exitingIndex = -1,
    isDirectUpdate = true
)

fun createRestoredBackStack(backstack: List<AnyOpenInstruction>) = NavigationContainerBackstack(
    backstack = backstack,
    exiting = null,
    exitingIndex = -1,
    lastInstruction = backstack.lastOrNull() ?: NavigationInstruction.Close,
    isDirectUpdate = true
)

data class NavigationContainerBackstack(
    val lastInstruction: NavigationInstruction,
    val backstack: List<AnyOpenInstruction>,
    val exiting: AnyOpenInstruction?,
    val exitingIndex: Int,
    val isDirectUpdate: Boolean
) {
    val visible: OpenPushInstruction? = backstack
        .lastOrNull { it.navigationDirection != NavigationDirection.Present } as? OpenPushInstruction

    val renderable: List<AnyOpenInstruction> = run {
        if (exiting == null) return@run backstack
        if (backstack.contains(exiting)) return@run backstack
        if (exitingIndex > backstack.lastIndex) return@run backstack + exiting
        return@run backstack.flatMapIndexed { index, open ->
            if (exitingIndex == index) return@flatMapIndexed listOf(exiting, open)
            return@flatMapIndexed listOf(open)
        }
    }

    internal fun push(
        vararg instructions: OpenPushInstruction
    ): NavigationContainerBackstack {
        if(instructions.isEmpty()) return this
        return copy(
            backstack = backstack + instructions,
            exiting = visible,
            exitingIndex = backstack.lastIndex,
            lastInstruction = instructions.last(),
            isDirectUpdate = false
        )
    }

    internal fun present(
        vararg instructions: OpenPresentInstruction
    ): NavigationContainerBackstack {
        if(instructions.isEmpty()) return this
        return copy(
            backstack = backstack + instructions,
            lastInstruction = instructions.last(),
            isDirectUpdate = false
        )
    }

    internal fun close(): NavigationContainerBackstack {
        return copy(
            backstack = backstack.dropLast(1),
            exiting = backstack.lastOrNull(),
            exitingIndex = backstack.lastIndex,
            lastInstruction = NavigationInstruction.Close,
            isDirectUpdate = false
        )
    }

    internal fun close(id: String): NavigationContainerBackstack {
        val index = backstack.indexOfLast {
            it.instructionId == id
        }
        if (index < 0) return this
        val exiting = backstack[index]
        return copy(
            backstack = backstack.minus(exiting),
            exiting = exiting,
            exitingIndex = index,
            lastInstruction = NavigationInstruction.Close,
            isDirectUpdate = false
        )
    }
}