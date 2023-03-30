package dev.enro.core.destinations

import android.os.Parcelable
import dev.enro.core.NavigationHandle
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestResult(
    val id: String
): Parcelable

private const val REGISTERED_TEST_RESULT = "dev.enro.core.destinations.registeredTestResult"
fun NavigationHandle.registerTestResult(result: TestResult) {
    instruction.additionalData[REGISTERED_TEST_RESULT] = result
}

fun NavigationHandle.hasTestResult(): Boolean {
    return instruction.additionalData.containsKey(REGISTERED_TEST_RESULT)
}
fun NavigationHandle.expectTestResult(): TestResult {
    return instruction.additionalData[REGISTERED_TEST_RESULT] as TestResult
}