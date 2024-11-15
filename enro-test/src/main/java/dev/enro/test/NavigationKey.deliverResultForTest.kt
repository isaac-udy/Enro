@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
package dev.enro.test

import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.result.EnroResult
import dev.enro.core.result.internal.PendingResult

/**
 * Given a NavigationKey.WithResult, this function will attempt to find the related NavigationInstruction that opened that
 * NavigationKey, and deliver a result to that instruction. This is useful for testing the behavior of a screen/ViewModel
 * that expects a result.
 */
fun <T : Any> NavigationKey.WithResult<T>.deliverResultForTest(
    type: Class<T>,
    result: T,
) {
    val exactInstruction = TestNavigationHandle.allInstructions
        .filterIsInstance<NavigationInstruction.Open<*>>()
        .firstOrNull {
            System.identityHashCode(it.navigationKey) == System.identityHashCode(this)
        }
    val fuzzyInstructions = TestNavigationHandle.allInstructions
        .filterIsInstance<NavigationInstruction.Open<*>>()
        .filter {
            it.navigationKey == this
        }
    if (fuzzyInstructions.isEmpty()) {
        throw EnroTestAssertionException("No instruction was found for NavigationKey $this")
    }
    val instruction = when {
        exactInstruction != null -> exactInstruction
        fuzzyInstructions.size == 1 -> fuzzyInstructions.first()
        else -> {
            throw EnroTestAssertionException("No instruction was found for NavigationKey $this")
        }
    }
    val navigationController = EnroTest.getCurrentNavigationController()
    val resultId = instruction.internal.resultId!!
    val navigationKey = instruction.internal.resultKey ?: instruction.navigationKey

    @Suppress("UNCHECKED_CAST")
    val pendingResult = PendingResult.Result(
        resultChannelId = resultId,
        instruction = instruction,
        navigationKey = navigationKey as NavigationKey.WithResult<T>,
        resultType = type.kotlin,
        result = result
    )
    EnroResult
        .from(navigationController)
        .addPendingResult(pendingResult)
}

/**
 * Given a NavigationKey.WithResult, this function will attempt to find the related NavigationInstruction that opened that
 * NavigationKey, and deliver a result to that instruction. This is useful for testing the behavior of a screen/ViewModel
 * that expects a result.
 */
inline fun <reified T : Any> NavigationKey.WithResult<T>.deliverResultForTest(result: T) {
    deliverResultForTest(T::class.java, result)
}