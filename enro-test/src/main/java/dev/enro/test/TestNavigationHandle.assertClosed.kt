package dev.enro.test

import dev.enro.core.NavigationInstruction

/**
 * Asserts that the NavigationHandle has received a RequestClose instruction
 */
fun TestNavigationHandle<*>.assertRequestedClose() {
    val instruction = instructions
        .filterIsInstance<NavigationInstruction.RequestClose>()
        .lastOrNull()

    instruction.shouldNotBeEqualTo(null) {
        "NavigationHandle was expected to have executed a RequestClose instruction, but no RequestClose instruction was found"
    }
}

/**
 * Asserts that the NavigationHandle has received a Close instruction
 */
fun TestNavigationHandle<*>.assertClosed() {
    val instruction = instructions.filterIsInstance<NavigationInstruction.Close>()
        .lastOrNull()

    instruction.shouldNotBeEqualTo(null) {
        "NavigationHandle was expected to have executed a Close instruction, but no Close instruction was found"
    }
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