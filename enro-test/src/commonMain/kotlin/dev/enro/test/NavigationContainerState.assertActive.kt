package dev.enro.test

import dev.enro.NavigationKey
import dev.enro.ui.NavigationContainerState
import kotlin.reflect.KClass

fun <T : NavigationKey> NavigationContainerState.assertActive(
    keyType: KClass<T>,
    predicate: (T) -> Boolean = { true }
) : NavigationKey.Instance<T> {
    val activeInstance = backstack.lastOrNull()
    activeInstance.shouldNotBeEqualTo(null) {
        "Expected $keyType to be the active NavigationKey, but the backstack is empty"
    }

    val activeKey = requireNotNull(activeInstance).key
    enroAssert(keyType.isInstance(activeKey)) {
        "Expected key of type ${keyType.simpleName} to be the active NavigationKey, but found $activeKey instead"
    }
    @Suppress("UNCHECKED_CAST")
    activeKey as T

    activeKey.shouldMatchPredicate(predicate) {
        "Expected $activeKey to match the provided predicate, but it did not"
    }
    @Suppress("UNCHECKED_CAST")
    return activeInstance as NavigationKey.Instance<T>
}

fun <T : NavigationKey> NavigationContainerState.assertActive(
    key: T,
) : NavigationKey.Instance<T> {
    return assertActive(key::class) { it == key }
}

inline fun <reified T : NavigationKey> NavigationContainerState.assertActive(
    noinline predicate: (T) -> Boolean = { true }
) : NavigationKey.Instance<T> {
    return assertActive(T::class, predicate)
}