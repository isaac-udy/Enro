@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro.test.extensions

import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.result.EnroResult
import dev.enro.core.result.internal.PendingResult
import dev.enro.test.EnroTest

/**
 * Given a NavigationInstruction.Open, this function will deliver a result to the instruction. This is useful for testing
 * the behavior of a screen/ViewModel that expects a result.
 */
@Deprecated("Use deliverResultForTest instead")
fun <T : Any> NavigationInstruction.Open<*>.sendResultForTest(type: Class<T>, result: T) {
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
@Deprecated("Use deliverResultForTest instead")
inline fun <reified T : Any> NavigationInstruction.Open<*>.sendResultForTest(result: T) {
    sendResultForTest(T::class.java, result)
}
