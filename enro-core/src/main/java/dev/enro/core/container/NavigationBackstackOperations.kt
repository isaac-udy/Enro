package dev.enro.core.container

import dev.enro.core.AdvancedEnroApi
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationInstruction

@AdvancedEnroApi
public fun NavigationContainer.setBackstack(
    block: (List<AnyOpenInstruction>) -> List<AnyOpenInstruction>
) {
    val initialState = backstackState
    val updatedBackstack = block(backstackState.backstack)

    val exitingIndex = updatedBackstack.indexOfLast { it.instructionId == initialState.active?.instructionId }
    val activeIndexInInitial = initialState.backstack.indexOfLast { it.instructionId == updatedBackstack.lastOrNull()?.instructionId }

    // The last instruction is considered to be a Close if the previously active item has been removed from the list,
    // and the newly active item is not present in the initial list
    val isClosing = exitingIndex == -1 && activeIndexInInitial != -1
    val updatedState = NavigationBackstackState(
        lastInstruction = when {
            isClosing -> NavigationInstruction.Close
            else -> updatedBackstack.lastOrNull() ?: NavigationInstruction.Close
        },
        backstack = updatedBackstack,
        exiting = initialState.active,
        exitingIndex = exitingIndex.takeIf { it > 0 } ?: initialState.backstack.lastIndex,
        updateType = NavigationBackstackState.UpdateType.STANDARD,
    )
    setBackstack(updatedState)
}

@AdvancedEnroApi
internal fun merge(
    oldBackstack: List<AnyOpenInstruction>,
    newBackstack: List<AnyOpenInstruction>,
): List<AnyOpenInstruction> {
    val results = mutableMapOf<Int, MutableList<AnyOpenInstruction>>()
    val indexes = mutableMapOf<AnyOpenInstruction, Int>()
    newBackstack.forEachIndexed { index, it ->
        results[index] = mutableListOf(it)
        indexes[it] = index
    }
    results[-1] = mutableListOf()

    var oldIndex = -1
    oldBackstack.forEach { oldItem ->
        oldIndex = maxOf(indexes[oldItem] ?: -1, oldIndex)
        results[oldIndex].let {
            if(it == null) return@let
            if(it.firstOrNull() == oldItem) return@let
            it.add(oldItem)
        }
    }

    return results.entries
        .sortedBy { it.key }
        .flatMap { it.value }
}

@AdvancedEnroApi
public fun isClosing(
    oldBackstack: List<AnyOpenInstruction>,
    newBackstack: List<AnyOpenInstruction>,
): Boolean {
    if (oldBackstack == newBackstack) return false
    val previousActive = oldBackstack.lastOrNull() ?: return false
    val newActive = newBackstack.lastOrNull() ?: return true
    return !newBackstack.contains(previousActive) && oldBackstack.contains(newActive)
}