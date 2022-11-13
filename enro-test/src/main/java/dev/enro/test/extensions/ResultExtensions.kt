@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
package dev.enro.test.extensions

import dev.enro.core.NavigationInstruction
import dev.enro.core.result.internal.EnroResult
import dev.enro.core.result.internal.PendingResult
import dev.enro.test.EnroTest

fun <T: Any> NavigationInstruction.Open<*>.sendResultForTest(type: Class<T>, result: T) {
    val navigationController = EnroTest.getCurrentNavigationController()
    val resultId = internal.resultId!!
    val pendingResult = PendingResult(
        resultId,
        type.kotlin,
        result
    )
    EnroResult
        .from(navigationController)
        .addPendingResult(pendingResult)
}

inline fun <reified T: Any> NavigationInstruction.Open<*>.sendResultForTest(result: T) {
    sendResultForTest(T::class.java, result)
}
