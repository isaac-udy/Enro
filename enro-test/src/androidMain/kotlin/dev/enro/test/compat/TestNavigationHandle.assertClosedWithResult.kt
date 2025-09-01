package dev.enro.test

import dev.enro.NavigationKey
import dev.enro.test.assertClosed
import dev.enro.test.assertNotClosed
import kotlin.reflect.KClass

@Deprecated("Use assertCompleted")
fun <T : Any> TestNavigationHandle<NavigationKey>.assertClosedWithResult(
    type: KClass<T>,
    predicate: (T) -> Boolean = { true },
) : T = assertCompleted(type, predicate)

/**
 * Asserts that the NavigationHandle has executed a Close.WithResult instruction, and that the result matches the provided predicate
 *
 * @return the result of the Close.WithResult instruction
 */
@Deprecated("Use assertCompleted")
inline fun <reified T : Any> TestNavigationHandle<NavigationKey>.assertClosedWithResult(
    noinline predicate: (T) -> Boolean = { true },
) : T = assertCompleted(predicate)

/**
 * Asserts that the NavigationHandle has executed a Close.WithResult instruction, and that the result matches the provided predicate
 *
 * @return the result of the Close.WithResult instruction
 */
@Deprecated("Use assertCompleted")
inline fun <reified T : Any> TestNavigationHandle<NavigationKey>.assertClosedWithResult(
    result: T,
) : T = assertCompleted(T::class) { it == result}


/**
 * Asserts that the NavigationHandle has executed a Close.WithResult instruction, and that the result is equal to [expected]
 */
@Deprecated("Use assertCompleted")
fun <T : Any> TestNavigationHandle<NavigationKey>.assertClosedWithResult(
    type: KClass<T>,
    expected: T,
): T = assertCompleted(type, expected)


/**
 * Asserts that the NavigationHandle has not executed a Close.WithResult instruction
 */
@Deprecated("Use assertNotClosed and assertNotCompleted")
fun TestNavigationHandle<NavigationKey>.assertNotClosedWithResult() {
    assertNotCompleted()
    assertNotClosed()
}


@Deprecated("Use assertNotClosed")
fun TestNavigationHandle<NavigationKey>.assertClosedWithNoResult() {
    assertClosed()
}