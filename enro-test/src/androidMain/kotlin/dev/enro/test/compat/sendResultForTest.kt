package dev.enro.test.extensions

import dev.enro.NavigationKey
import dev.enro.asCompleteOperation

fun <T : Any> NavigationKey.Instance<NavigationKey.WithResult<T>>.sendResultForTest(
    result: T
) {
    asCompleteOperation(result).registerResult()
}