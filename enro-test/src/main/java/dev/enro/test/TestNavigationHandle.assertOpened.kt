package dev.enro.test

import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import org.junit.Assert

fun <T : Any> TestNavigationHandle<*>.assertOpened(type: Class<T>, direction: NavigationDirection? = null): T {
    val instruction = instructions.filterIsInstance<NavigationInstruction.Open<*>>()
        .lastOrNull()

    Assert.assertNotNull(instruction)
    requireNotNull(instruction)

    Assert.assertTrue(type.isAssignableFrom(instruction.navigationKey::class.java))
    if (direction != null) {
        Assert.assertEquals(direction, instruction.navigationDirection)
    }
    return instruction.navigationKey as T
}

inline fun <reified T : Any> TestNavigationHandle<*>.assertOpened(direction: NavigationDirection? = null): T {
    return assertOpened(T::class.java, direction)
}

fun <T : Any> TestNavigationHandle<*>.assertAnyOpened(type: Class<T>, direction: NavigationDirection? = null): T {
    val instruction = instructions.filterIsInstance<NavigationInstruction.Open<*>>()
        .lastOrNull { type.isAssignableFrom(it.navigationKey::class.java) }

    Assert.assertNotNull(instruction)
    requireNotNull(instruction)

    Assert.assertTrue(type.isAssignableFrom(instruction.navigationKey::class.java))
    if (direction != null) {
        Assert.assertEquals(direction, instruction.navigationDirection)
    }
    return instruction.navigationKey as T
}

inline fun <reified T : Any> TestNavigationHandle<*>.assertAnyOpened(direction: NavigationDirection? = null): T {
    return assertAnyOpened(T::class.java, direction)
}

fun TestNavigationHandle<*>.assertNoneOpened() {
    val instruction = instructions.filterIsInstance<NavigationInstruction.Open<*>>()
        .lastOrNull()
    Assert.assertNull(instruction)
}