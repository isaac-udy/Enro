package dev.enro.core.container

import dev.enro.core.AdvancedEnroApi
import dev.enro.core.AnyOpenInstruction

@AdvancedEnroApi
public fun NavigationContainer.setBackstack(
    block: (List<AnyOpenInstruction>) -> List<AnyOpenInstruction>
) {
    setBackstack(block(backstack))
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