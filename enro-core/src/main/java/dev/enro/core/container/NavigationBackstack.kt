package dev.enro.core.container

import dev.enro.core.*

fun createEmptyBackStack() = NavigationBackstack(
    lastInstruction = NavigationInstruction.Close,
    backstack = emptyList(),
    exiting = null,
    exitingIndex = -1,
    isDirectUpdate = true
)

fun createRootBackStack(rootInstruction: AnyOpenInstruction?) = NavigationBackstack(
    lastInstruction = NavigationInstruction.Close,
    backstack = listOfNotNull(rootInstruction),
    exiting = null,
    exitingIndex = -1,
    isDirectUpdate = true
)

fun createRootBackStack(backstack: List<AnyOpenInstruction>) = NavigationBackstack(
    lastInstruction = backstack.lastOrNull() ?: NavigationInstruction.Close,
    backstack = backstack,
    exiting = null,
    exitingIndex = -1,
    isDirectUpdate = true
)

fun createRestoredBackStack(backstack: List<AnyOpenInstruction>) = NavigationBackstack(
    backstack = backstack,
    exiting = null,
    exitingIndex = -1,
    lastInstruction = backstack.lastOrNull() ?: NavigationInstruction.Close,
    isDirectUpdate = true
)

data class NavigationBackstack(
    val lastInstruction: NavigationInstruction,
    val backstack: List<AnyOpenInstruction>,
    val exiting: AnyOpenInstruction?,
    val exitingIndex: Int,
    val isDirectUpdate: Boolean
) {
    val active: AnyOpenInstruction? = backstack.lastOrNull()

    val renderable: List<AnyOpenInstruction> = run {
        if (exiting == null) return@run backstack
        if (backstack.contains(exiting)) return@run backstack
        if (exitingIndex > backstack.lastIndex) return@run backstack + exiting
        return@run backstack.flatMapIndexed { index, open ->
            if (exitingIndex == index) return@flatMapIndexed listOf(exiting, open)
            return@flatMapIndexed listOf(open)
        }
    }
}

internal fun NavigationBackstack.add(
    vararg instructions: AnyOpenInstruction
): NavigationBackstack {
    if(instructions.isEmpty()) return this
    return copy(
        backstack = backstack + instructions,
        exiting = active,
        exitingIndex = backstack.lastIndex,
        lastInstruction = instructions.last(),
        isDirectUpdate = false
    )
}

internal fun NavigationBackstack.close(): NavigationBackstack {
    return copy(
        backstack = backstack.dropLast(1),
        exiting = backstack.lastOrNull(),
        exitingIndex = backstack.lastIndex,
        lastInstruction = NavigationInstruction.Close,
        isDirectUpdate = false
    )
}

internal fun NavigationBackstack.close(id: String): NavigationBackstack {
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