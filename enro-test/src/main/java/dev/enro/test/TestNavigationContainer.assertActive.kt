package dev.enro.test

import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.container.NavigationContainerContext
import org.junit.Assert

fun NavigationContainerContext.assertActive(
    instruction: NavigationInstruction.Open<*>
) {
    Assert.assertEquals(instruction, backstack.active)
}

fun NavigationContainerContext.assertActive(
    key: NavigationKey
) {
    Assert.assertEquals(key, backstack.active?.navigationKey)
}

fun NavigationContainerContext.assertNotActive(
    instruction: NavigationInstruction.Open<*>
) {
    Assert.assertNotEquals(instruction, backstack.active)
}

fun NavigationContainerContext.assertNotActive(
    key: NavigationKey
) {
    Assert.assertNotEquals(key, backstack.active?.navigationKey)
}