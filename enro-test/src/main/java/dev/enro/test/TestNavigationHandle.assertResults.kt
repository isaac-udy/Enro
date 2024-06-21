package dev.enro.test

import dev.enro.core.NavigationInstruction
import org.junit.Assert

internal fun TestNavigationHandle<*>.getResult(): Any? {
    return instructions.filterIsInstance<NavigationInstruction.Close.WithResult>()
        .lastOrNull()
        ?.result
}

@Deprecated("Use assertClosedWithResult")
fun <T : Any> TestNavigationHandle<*>.assertResultDelivered(predicate: (T) -> Boolean): T {
    val result = getResult()
    Assert.assertNotNull(result)
    requireNotNull(result)
    result as T
    Assert.assertTrue(predicate(result))
    return result
}

@Deprecated("Use assertClosedWithResult")
fun <T : Any> TestNavigationHandle<*>.assertResultDelivered(expected: T): T {
    val result = getResult()
    Assert.assertEquals(expected, result)
    return result as T
}

@Deprecated("Use assertClosedWithResult")
inline fun <reified T : Any> TestNavigationHandle<*>.assertResultDelivered(): T {
    return assertResultDelivered { true }
}

@Deprecated("Use assertNotClosedWithResult")
fun TestNavigationHandle<*>.assertNoResultDelivered() {
    val result = getResult()
    Assert.assertNull(result)
}