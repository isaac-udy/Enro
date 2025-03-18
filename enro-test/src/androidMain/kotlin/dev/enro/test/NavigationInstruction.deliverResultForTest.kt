@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
package dev.enro.test

import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.result.EnroResult
import dev.enro.core.result.internal.PendingResult

/**
 * Given a NavigationInstruction.Open, this function will deliver a result to the instruction. This is useful for testing
 * the behavior of a screen/ViewModel that expects a result.
 */
fun <T : Any> NavigationInstruction.Open<*>.deliverResultForTest(type: Class<T>, result: T) {
    val navigationController = EnroTest.getCurrentNavigationController()
    val resultId = internal.resultId!!

    val navigationKey = internal.resultKey ?: navigationKey

    val pendingResult = PendingResult.Result(
        resultChannelId = resultId,
        instruction = this,
        navigationKey = navigationKey as NavigationKey.WithResult<T>,
        resultType = type.kotlin,
        result = result
    )
    EnroResult
        .from(navigationController)
        .addPendingResult(pendingResult)
}

/**
 * Given a NavigationInstruction.Open, this function will deliver a result to the instruction. This is useful for testing
 * the behavior of a screen/ViewModel that expects a result.
 */
inline fun <reified T : Any> NavigationInstruction.Open<*>.deliverResultForTest(result: T) {
    deliverResultForTest(T::class.java, result)
}