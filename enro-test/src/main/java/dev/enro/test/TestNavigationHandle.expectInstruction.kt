package dev.enro.test

import dev.enro.core.NavigationInstruction
import junit.framework.TestCase
import org.junit.Assert

fun TestNavigationHandle<*>.expectCloseInstruction() {
    TestCase.assertTrue(instructions.last() is NavigationInstruction.Close)
}

fun <T : Any> TestNavigationHandle<*>.expectOpenInstruction(type: Class<T>, filter: (T) -> Boolean = { true }): NavigationInstruction.Open<*> {
    val instruction = instructions.filterIsInstance<NavigationInstruction.Open<*>>().last {
        runCatching { filter(it.navigationKey as T) }.getOrDefault(false)
    }
    Assert.assertTrue(type.isAssignableFrom(instruction.navigationKey::class.java))
    return instruction
}

inline fun <reified T : Any> TestNavigationHandle<*>.expectOpenInstruction(noinline filter: (T) -> Boolean = { true }): NavigationInstruction.Open<*> {
    return expectOpenInstruction(T::class.java, filter)
}

inline fun <reified T : Any> TestNavigationHandle<*>.expectOpenInstruction(key: T): NavigationInstruction.Open<*> {
    return expectOpenInstruction(T::class.java) { it == key }
}