@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package dev.enro.test

import dev.enro.NavigationKey
import dev.enro.NavigationOperation

/**
 * Asserts that the NavigationHandle's instance has been closed
 */
fun TestNavigationHandle<NavigationKey>.assertClosed() {
    val last = operations.lastOrNull()
    enroAssert(last != null) {
        "Expected the last operation to be a close operation, but there were no operations"
    }
    enroAssert(last is NavigationOperation.Close<*>) {
        "Expected the last operation to be a close operation, but was ${last::class.simpleName}"
    }
    require(last.instance.id == instance.id) {
        "Expected the last operation to be a close operation for this NavigationHandle's instance ${instance.id}, but was ${last.instance.id}"
    }
}

fun TestNavigationHandle<NavigationKey>.assertNotClosed() {
    val last = operations.lastOrNull()
    if (last !is NavigationOperation.Close<*>) return
    require(last.instance.id != instance.id) {
        "Expected the last operation to not be a close operation for instance ${instance.id}"
    }
}
