package dev.enro.core.container

import android.os.Parcelable
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import kotlinx.parcelize.Parcelize

@Parcelize
data class NavigationContainerBackstackEntry(
    val instruction: NavigationInstruction.Open,
    val previouslyActiveContainerId: String?
) : Parcelable

fun createEmptyBackStack() = NavigationContainerBackstack(
    lastInstruction = NavigationInstruction.Close,
    backstackEntries = listOf(),
    exiting = null,
    exitingIndex = -1,
    isDirectUpdate = true
)

fun createRestoredBackStack(backstackEntries: List<NavigationContainerBackstackEntry>) = NavigationContainerBackstack(
    backstackEntries = backstackEntries,
    exiting = null,
    exitingIndex = -1,
    lastInstruction = backstackEntries.lastOrNull()?.instruction ?: NavigationInstruction.Close,
    isDirectUpdate = true
)

data class NavigationContainerBackstack(
    val lastInstruction: NavigationInstruction,
    val backstackEntries: List<NavigationContainerBackstackEntry>,
    val exiting: NavigationInstruction.Open?,
    val exitingIndex: Int,
    val isDirectUpdate: Boolean
) {
    val backstack = backstackEntries.map { it.instruction }
    val visible: NavigationInstruction.Open? = backstack.lastOrNull()
    val renderable: List<NavigationInstruction.Open> = run {
        if(exiting == null) return@run backstack
        if(backstack.contains(exiting)) return@run backstack
        if(exitingIndex > backstack.lastIndex) return@run backstack + exiting
        return@run backstack.flatMapIndexed { index, open ->
            if(exitingIndex == index) return@flatMapIndexed listOf(exiting, open)
            return@flatMapIndexed listOf(open)
        }
    }

    internal fun push(
        instruction: NavigationInstruction.Open,
        activeContainerId: String?
    ): NavigationContainerBackstack {
        return when (instruction.navigationDirection) {
            NavigationDirection.FORWARD -> {
                copy(
                    backstackEntries = backstackEntries + NavigationContainerBackstackEntry(
                        instruction,
                        activeContainerId
                    ),
                    exiting = visible,
                    exitingIndex = backstack.lastIndex,
                    lastInstruction = instruction,
                    isDirectUpdate = false
                )
            }
            NavigationDirection.REPLACE -> {
                copy(
                    backstackEntries = backstackEntries.dropLast(1) + NavigationContainerBackstackEntry(
                        instruction,
                        activeContainerId
                    ),
                    exiting = visible,
                    exitingIndex = backstack.lastIndex,
                    lastInstruction = instruction,
                    isDirectUpdate = false
                )
            }
            NavigationDirection.REPLACE_ROOT -> {
                copy(
                    backstackEntries = listOf(
                        NavigationContainerBackstackEntry(
                            instruction,
                            activeContainerId
                        )
                    ),
                    exiting = visible,
                    exitingIndex = 0,
                    lastInstruction = instruction,
                    isDirectUpdate = false
                )
            }
        }
    }

    internal fun close(): NavigationContainerBackstack {
        return copy(
            backstackEntries = backstackEntries.dropLast(1),
            exiting = visible,
            exitingIndex = backstack.lastIndex,
            lastInstruction = NavigationInstruction.Close,
            isDirectUpdate = false
        )
    }

    internal fun close(id: String): NavigationContainerBackstack {
        val index = backstackEntries.indexOfLast {
            it.instruction.instructionId == id
        }
        if(index < 0) return this
        val exiting = backstackEntries.get(index)
        return copy(
            backstackEntries = backstackEntries.minus(exiting),
            exiting = exiting.instruction,
            exitingIndex = index,
            lastInstruction = NavigationInstruction.Close,
            isDirectUpdate = false
        )
    }
}