package dev.enro.core.compatability

import dev.enro.core.ExecutorArgs
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.container.asPresentInstruction
import dev.enro.core.container.asPushInstruction

internal fun ExecutorArgs<*, *, *>.getInstructionForCompatibility(): NavigationInstruction.Open<*> {
    val isDialog = isDialog()
    return when (instruction.navigationDirection) {
        is NavigationDirection.Replace,
        is NavigationDirection.Forward -> when {
            isDialog -> instruction.asPresentInstruction()
            else -> instruction.asPushInstruction()
        }
        else -> instruction
    }
}