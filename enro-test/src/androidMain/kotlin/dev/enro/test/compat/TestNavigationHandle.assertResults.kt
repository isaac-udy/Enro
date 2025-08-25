package dev.enro.test



@Deprecated("Use assertClosedWithResult")
@Suppress("UNCHECKED_CAST")
fun <T : Any> TestNavigationHandle<*>.assertResultDelivered(predicate: (T) -> Boolean): T {
    return assertCompleted<Any> {
        @Suppress("SafeCastWithReturn")
        it as? T ?: return@assertCompleted false
        predicate(it)
    } as T
}

@Deprecated("Use assertClosedWithResult")
@Suppress("UNCHECKED_CAST")
fun <T : Any> TestNavigationHandle<*>.assertResultDelivered(expected: T): T {
    return assertCompleted<Any>(expected) as T
}

@Deprecated("Use assertClosedWithResult")
inline fun <reified T : Any> TestNavigationHandle<*>.assertResultDelivered(): T {
    return assertResultDelivered { true }
}

@Deprecated("Use assertNotClosedWithResult")
fun TestNavigationHandle<*>.assertNoResultDelivered() {
    assertNotCompleted()
}