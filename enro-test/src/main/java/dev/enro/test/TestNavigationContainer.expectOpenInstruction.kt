package dev.enro.test

import dev.enro.core.NavigationInstruction
import dev.enro.core.container.NavigationContainerContext
import org.junit.Assert

fun <T : Any> NavigationContainerContext.expectOpenInstruction(type: Class<T>, filter: (T) -> Boolean = { true }): NavigationInstruction.Open<*> {
    val instruction = backstack.last {
        runCatching { filter(it.navigationKey as T) }.getOrDefault(false)
    }
    Assert.assertTrue(type.isAssignableFrom(instruction.navigationKey::class.java))
    return instruction
}

inline fun <reified T : Any> NavigationContainerContext.expectOpenInstruction(noinline filter: (T) -> Boolean = { true }): NavigationInstruction.Open<*> {
    return expectOpenInstruction(T::class.java, filter)
}

inline fun <reified T : Any> NavigationContainerContext.expectOpenInstruction(key: T): NavigationInstruction.Open<*> {
    return expectOpenInstruction(T::class.java) { it == key }
}