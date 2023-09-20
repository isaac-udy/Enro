package dev.enro.test

import dev.enro.core.NavigationInstruction
import org.junit.Assert

fun TestNavigationHandle<*>.assertRequestedClose() {
    val instruction = instructions.filterIsInstance<NavigationInstruction.RequestClose>()
        .lastOrNull()
    Assert.assertNotNull(instruction)
}

fun TestNavigationHandle<*>.assertClosed() {
    val instruction = instructions.filterIsInstance<NavigationInstruction.Close>()
        .lastOrNull()
    Assert.assertNotNull(instruction)
}

fun TestNavigationHandle<*>.assertNotClosed() {
    val instruction = instructions.filterIsInstance<NavigationInstruction.Close>()
        .lastOrNull()
    Assert.assertNull(instruction)
}