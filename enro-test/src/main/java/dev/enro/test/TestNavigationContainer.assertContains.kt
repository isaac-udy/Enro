package dev.enro.test

import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.container.NavigationContainerContext
import org.junit.Assert

fun NavigationContainerContext.assertContains(
    instruction: NavigationInstruction.Open<*>
) {
    Assert.assertEquals(
        instruction,
        backstack.firstOrNull { it == instruction }
    )
}

fun NavigationContainerContext.assertContains(
    key: NavigationKey
) {
    Assert.assertEquals(
        key,
        backstack.map { it.navigationKey }
            .firstOrNull { it == key }
    )
}

fun NavigationContainerContext.assertDoesNotContain(
    instruction: NavigationInstruction.Open<*>
) {
    Assert.assertEquals(
        null,
        backstack.firstOrNull { it == instruction }
    )
}

fun NavigationContainerContext.assertDoesNotContain(
    key: NavigationKey
) {
    Assert.assertEquals(
        null,
        backstack.map { it.navigationKey }
            .firstOrNull { it == key }
    )
}