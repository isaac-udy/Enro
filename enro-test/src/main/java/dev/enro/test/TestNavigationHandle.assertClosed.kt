package dev.enro.test

import dev.enro.core.NavigationInstruction

/**
 * Asserts that the NavigationHandle has received a RequestClose instruction
 */
fun TestNavigationHandle<*>.assertRequestedClose() : NavigationInstruction.RequestClose {
    val instruction = instructions
        .filterIsInstance<NavigationInstruction.RequestClose>()
        .lastOrNull()

    instruction.shouldNotBeEqualTo(null) {
        "NavigationHandle was expected to have executed a RequestClose instruction, but no RequestClose instruction was found"
    }
    return instruction!!
}

/**
 * Asserts that the NavigationHandle has received a Close instruction
 *
 * @return the Close instruction that was executed
 */
fun TestNavigationHandle<*>.assertClosed() : NavigationInstruction.Close {
    val instruction = instructions.filterIsInstance<NavigationInstruction.Close>()
        .lastOrNull()

    instruction.shouldNotBeEqualTo(null) {
        "NavigationHandle was expected to have executed a Close instruction, but no Close instruction was found"
    }
    return instruction!!
}

/**
 * Asserts that the NavigationHandle has not received a Close instruction
 */
fun TestNavigationHandle<*>.assertNotClosed() {
    val instruction = instructions.filterIsInstance<NavigationInstruction.Close>()
        .lastOrNull()

    instruction.shouldBeEqualTo(null) {
        "NavigationHandle should not have executed a Close instruction, but a Close instruction was found"
    }
}