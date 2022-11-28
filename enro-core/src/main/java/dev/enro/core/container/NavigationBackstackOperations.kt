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
        exitingIndex = exitingIndex,
        updateType = NavigationBackstackState.UpdateType.STANDARD,
    )
    setBackstack(updatedState)
}