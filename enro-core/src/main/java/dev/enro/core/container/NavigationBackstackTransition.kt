package dev.enro.core.container

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationInstruction

@Stable
@Immutable
public class NavigationBackstackTransition(
    value: Pair<NavigationBackstack, NavigationBackstack>
) {
    public val previousBackstack: NavigationBackstack = value.first
    public val activeBackstack: NavigationBackstack = value.second

    private val currentlyActiveIndexInPrevious = previousBackstack.indexOfLast { it.instructionId == activeBackstack.active?.instructionId }
    private val previouslyActiveIndexInBackstack = activeBackstack.indexOfLast { it.instructionId == previousBackstack.active?.instructionId }

    private val previouslyActiveCountInPrevious = previousBackstack.count { it.instructionId == previousBackstack.active?.instructionId }
    private val previouslyActiveCountInActive = activeBackstack.count { it.instructionId == previousBackstack.active?.instructionId }

    // The last instruction is considered to be a Close if the previously active item has been removed from the list,
    // and the newly active item is not present in the initial list
    private val isClosing = (previouslyActiveIndexInBackstack == -1 && currentlyActiveIndexInPrevious != -1)

    public val lastInstruction: NavigationInstruction =  when {
        isClosing -> NavigationInstruction.Close
        else -> activeBackstack.lastOrNull() ?: NavigationInstruction.Close
    }

    public val exitingIndex: Int = previouslyActiveIndexInBackstack.takeIf { it > 0 } ?: previousBackstack.lastIndex
    public val exitingInstruction: AnyOpenInstruction? = when {
        previousBackstack.active != activeBackstack.active -> previousBackstack.active
        else -> null
    }

    public val removed: List<AnyOpenInstruction> by lazy {
        val active = activeBackstack.associateBy { it.instructionId }
        previousBackstack.filter {
            active[it.instructionId] == null
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NavigationBackstackTransition

        if (previousBackstack != other.previousBackstack) return false
        if (activeBackstack != other.activeBackstack) return false
        if (currentlyActiveIndexInPrevious != other.currentlyActiveIndexInPrevious) return false
        if (previouslyActiveIndexInBackstack != other.previouslyActiveIndexInBackstack) return false
        if (previouslyActiveCountInPrevious != other.previouslyActiveCountInPrevious) return false
        if (previouslyActiveCountInActive != other.previouslyActiveCountInActive) return false
        if (isClosing != other.isClosing) return false
        if (lastInstruction != other.lastInstruction) return false
        if (exitingIndex != other.exitingIndex) return false
        return exitingInstruction == other.exitingInstruction
    }

    override fun hashCode(): Int {
        var result = previousBackstack.hashCode()
        result = 31 * result + activeBackstack.hashCode()
        result = 31 * result + currentlyActiveIndexInPrevious
        result = 31 * result + previouslyActiveIndexInBackstack
        result = 31 * result + previouslyActiveCountInPrevious
        result = 31 * result + previouslyActiveCountInActive
        result = 31 * result + isClosing.hashCode()
        result = 31 * result + lastInstruction.hashCode()
        result = 31 * result + exitingIndex
        result = 31 * result + (exitingInstruction?.hashCode() ?: 0)
        return result
    }


}