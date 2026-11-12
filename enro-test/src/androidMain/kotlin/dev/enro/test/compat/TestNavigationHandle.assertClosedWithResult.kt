package dev.enro.test

import dev.enro.NavigationKey
import dev.enro.test.assertClosed
import dev.enro.test.assertNotClosed
import kotlin.reflect.KClass

@Deprecated("Use assertCompleted")
public fun <T : Any> TestNavigationHandle<NavigationKey>.assertClosedWithResult(
    type: KClass<T>,
    predicate: (T) -> Boolean = { true },
) : T = assertCompleted(type, predicate)

@Deprecated("Use assertCompleted")
public inline fun <reified T : Any> TestNavigationHandle<NavigationKey>.assertClosedWithResult(
    noinline predicate: (T) -> Boolean = { true },
) : T = assertCompleted(predicate)

@Deprecated("Use assertCompleted")
public inline fun <reified T : Any> TestNavigationHandle<NavigationKey>.assertClosedWithResult(
    result: T,
) : T = assertCompleted(T::class) { it == result}


@Deprecated("Use assertCompleted")
public fun <T : Any> TestNavigationHandle<NavigationKey>.assertClosedWithResult(
    type: KClass<T>,
    expected: T,
): T = assertCompleted(type, expected)


@Deprecated("Use assertNotClosed and assertNotCompleted")
public fun TestNavigationHandle<NavigationKey>.assertNotClosedWithResult() {
    assertNotCompleted()
    assertNotClosed()
}


@Deprecated("Use assertNotClosed")
public fun TestNavigationHandle<NavigationKey>.assertClosedWithNoResult() {
    assertClosed()
}