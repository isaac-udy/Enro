package dev.enro.core.container

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationInstruction

public class NavigationBackstackTransition(
    value: Pair<NavigationBackstack, NavigationBackstack>
) {
    public val previousBackstack: NavigationBackstack = value.first
    public val activeBackstack: NavigationBackstack = value.second

    private val currentlyActiveIndexInPrevious = previousBackstack.indexOfLast { it.instructionId == activeBackstack.active?.instructionId }
    private val previouslyActiveIndexInBackstack = activeBackstack.indexOfLast { it.instructionId == previousBackstack.active?.instructionId }

    // The last instruction is considered to be a Close if the previously active item has been removed from the list,
    // and the newly active item is not present in the initial list
    private val isClosing = previouslyActiveIndexInBackstack == -1 && currentlyActiveIndexInPrevious != -1

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
}