package dev.enro.core.destinations

import android.os.Parcelable
import androidx.savedstate.read
import androidx.savedstate.write
import dev.enro.core.NavigationHandle
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestResult(
    val id: String
): Parcelable

private const val REGISTERED_TEST_RESULT = "dev.enro.core.destinations.registeredTestResult"
fun NavigationHandle.registerTestResult(result: TestResult) {
    instruction.extras.write {
        putParcelable(REGISTERED_TEST_RESULT, result)
    }
}

fun NavigationHandle.hasTestResult(): Boolean {
    return instruction.extras.containsKey(REGISTERED_TEST_RESULT)
}
fun NavigationHandle.expectTestResult(): TestResult {
    return instruction.extras.read { getParcelable(REGISTERED_TEST_RESULT) }
}