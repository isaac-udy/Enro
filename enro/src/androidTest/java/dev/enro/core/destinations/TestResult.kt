package dev.enro.core.destinations

import android.os.Parcelable
import androidx.lifecycle.ViewModelProvider
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.result.EnroResultChannel
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestResult(
    val id: String
): Parcelable

private const val REGISTERED_TEST_RESULT = "dev.enro.core.destinations.registeredTestResult"
fun NavigationHandle.registerTestResult(result: TestResult) {
    additionalData.putParcelable(
        REGISTERED_TEST_RESULT,
        result
    )
}

fun NavigationHandle.hasTestResult(): Boolean {
    return additionalData.containsKey(REGISTERED_TEST_RESULT)
}
fun NavigationHandle.expectTestResult(): TestResult {
    return additionalData.getParcelable<TestResult>(REGISTERED_TEST_RESULT) ?: throw IllegalStateException()
}