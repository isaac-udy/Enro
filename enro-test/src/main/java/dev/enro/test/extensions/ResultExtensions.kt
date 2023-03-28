@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
package dev.enro.test.extensions

import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.result.EnroResult
import dev.enro.core.result.internal.PendingResult
import dev.enro.extensions.getParcelableCompat
import dev.enro.test.EnroTest

fun <T: Any> NavigationInstruction.Open<*>.sendResultForTest(type: Class<T>, result: T) {
    val navigationController = EnroTest.getCurrentNavigationController()
    val resultId = internal.resultId!!

    val navigationKey = additionalData.getParcelableCompat(PendingResult.OVERRIDE_NAVIGATION_KEY_EXTRA)
        ?: navigationKey

    val pendingResult = PendingResult.Result(
        resultId,
        navigationKey as NavigationKey.WithResult<T>,
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
