package dev.enro.core.destinations

import android.os.Parcelable
import androidx.savedstate.serialization.serializers.ParcelableSerializer
import dev.enro.core.NavigationHandle
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestResult(
    val id: String
): Parcelable

internal object TestResultSerializer : ParcelableSerializer<TestResult>()

private const val REGISTERED_TEST_RESULT = "dev.enro.core.destinations.registeredTestResult"
fun NavigationHandle.registerTestResult(result: TestResult) {
    instruction.extras.put(REGISTERED_TEST_RESULT, result)
}

fun NavigationHandle.hasTestResult(): Boolean {
    return instruction.extras.values.containsKey(REGISTERED_TEST_RESULT)
}
fun NavigationHandle.expectTestResult(): TestResult {
    return instruction.extras.get(REGISTERED_TEST_RESULT) ?: throw IllegalStateException(
        "Expected test result to be registered, but none was found"
    )
}