package dev.enro.core.compose

import android.os.Parcelable
import androidx.compose.runtime.saveable.Saver
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import kotlinx.parcelize.Parcelize

@Parcelize
data class EnroContainerBackstackEntry(
    val instruction: NavigationInstruction.Open,
    val previouslyActiveContainerId: String?
) : Parcelable

data class EnroContainerBackstackState(
    val lastInstruction: NavigationInstruction,
    val backstackEntries: List<EnroContainerBackstackEntry>,
    val exiting: NavigationInstruction.Open?,
    val exitingIndex: Int,
    val skipAnimations: Boolean
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
    ): EnroContainerBackstackState {
        return when (instruction.navigationDirection) {
            NavigationDirection.FORWARD -> {
                copy(
                    backstackEntries = backstackEntries + EnroContainerBackstackEntry(
                        instruction,
                        activeContainerId
                    ),
                    exiting = visible,
                    exitingIndex = backstack.lastIndex,
                    lastInstruction = instruction,
                    skipAnimations = false
                )
            }
            NavigationDirection.REPLACE -> {
                copy(
                    backstackEntries = backstackEntries.dropLast(1) + EnroContainerBackstackEntry(
                        instruction,
                        activeContainerId
                    ),
                    exiting = visible,
                    exitingIndex = backstack.lastIndex,
                    lastInstruction = instruction,
                    skipAnimations = false
                )
            }
            NavigationDirection.REPLACE_ROOT -> {
                copy(
                    backstackEntries = listOf(
                        EnroContainerBackstackEntry(
                            instruction,
                            activeContainerId
                        )
                    ),
                    exiting = visible,
                    exitingIndex = 0,
                    lastInstruction = instruction,
                    skipAnimations = false
                )
            }
        }
    }

    internal fun close(): EnroContainerBackstackState {
        return copy(
            backstackEntries = backstackEntries.dropLast(1),
            exiting = visible,
            exitingIndex = backstack.lastIndex,
            lastInstruction = NavigationInstruction.Close,
            skipAnimations = false
        )
    }
}

fun createEnroContainerBackstackStateSaver(
    getCurrentState: () -> EnroContainerBackstackState?
) = Saver<EnroContainerBackstackState, ArrayList<EnroContainerBackstackEntry>> (
    save = { value ->
        val entries = getCurrentState()?.backstackEntries ?: value.backstackEntries
        return@Saver ArrayList(entries)
    },
    restore = { value ->
        return@Saver EnroContainerBackstackState(
            backstackEntries = value,
            exiting = null,
            exitingIndex = -1,
            lastInstruction = value.lastOrNull()?.instruction ?: NavigationInstruction.Close,
            skipAnimations = true
        )
    }
)