// Snippets for docs/ghpages/docs/advanced/testing.md
package dev.enro.docs.advanced.testing

import androidx.lifecycle.ViewModel
import dev.enro.NavigationKey
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.test.TestNavigationHandle
import dev.enro.test.assertActive
import dev.enro.test.assertClosed
import dev.enro.test.assertCompleted
import dev.enro.test.assertNoneOpened
import dev.enro.test.assertNotClosed
import dev.enro.test.assertNotCompleted
import dev.enro.test.assertOpened
import dev.enro.test.createTestNavigationHandle
import dev.enro.test.putNavigationHandleForViewModel
import dev.enro.test.runEnroTest
import dev.enro.test.sendClosedForTest
import dev.enro.test.sendCompletedForTest
import dev.enro.test.sendResultForTest
import dev.enro.ui.NavigationContainerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

private val today = LocalDate(2026, 1, 1)

@Serializable
data class ShowProfile(val userId: String) : NavigationKey

@Serializable
data class EditProfile(val initial: String) : NavigationKey

@Serializable
data class SelectDate(val maxDate: LocalDate? = null) : NavigationKey.WithResult<LocalDate>

@Serializable
data object ConfirmDestination : NavigationKey.WithResult<String>

@Serializable
data object AcknowledgeDialog : NavigationKey

@Serializable
data object DatePicker : NavigationKey.WithResult<LocalDate>

class EditProfileViewModel : ViewModel() {
    private val navigation by navigationHandle<EditProfile>()

    fun onSaveClicked() {
        navigation.complete()
    }
}

// region docs:testing/run-enro-test
@Test
fun open_profile_navigates_to_edit_screen() = runEnroTest {
    val handle = createTestNavigationHandle(ShowProfile("user-123"))

    handle.open(EditProfile("user-123"))

    handle.assertOpened<EditProfile>()
}
// endregion

fun testNavigationHandle() {
    // region docs:testing/test-navigation-handle
    val handle = createTestNavigationHandle(ShowProfile("user-123"))

    handle.open(SelectDate())
    handle.complete()

    // both operations are recorded on the handle
    // endregion
}

fun assertOpened(handle: TestNavigationHandle<ShowProfile>) {
    // region docs:testing/assert-opened
    // Any open of type T
    val instance = handle.assertOpened<EditProfile>()

    // A specific key value
    handle.assertOpened(EditProfile("user-123"))

    // A predicate over the instance
    handle.assertOpened<EditProfile> { it.key.initial == "user-123" }

    // "Nothing was opened"
    handle.assertNoneOpened()
    // endregion
}

fun assertClosedAndCompleted(handle: TestNavigationHandle<SelectDate>) {
    // region docs:testing/assert-closed-completed
    handle.assertClosed()
    handle.assertNotClosed()

    handle.assertCompleted()
    handle.assertCompleted(today)                      // with a specific result
    handle.assertCompleted<LocalDate> { it.year > 2020 }
    handle.assertNotCompleted()
    // endregion
}

fun containerAssertions(state: NavigationContainerState) {
    // region docs:testing/container-assertions
    state.assertActive<ShowProfile>()
    state.assertActive(ShowProfile("user-123"))
    state.assertActive<ShowProfile> { it.userId == "user-123" }
    // endregion
}

// region docs:testing/view-model
@Test
fun saving_changes_completes_the_destination() = runEnroTest {
    val handle = putNavigationHandleForViewModel<EditProfileViewModel, EditProfile>(
        EditProfile(initial = "Hello"),
    )

    val viewModel = EditProfileViewModel()
    viewModel.onSaveClicked()

    handle.assertCompleted()
}
// endregion

fun childResults(handle: TestNavigationHandle<ShowProfile>) {
    // region docs:testing/child-results
    // sendResultForTest — typed result
    val child = handle.assertOpened<ConfirmDestination>()
    child.sendResultForTest("confirmed")

    // sendCompletedForTest — Unit/no-payload completion
    val dialog = handle.assertOpened<AcknowledgeDialog>()
    dialog.sendCompletedForTest()

    // sendClosedForTest — the child was dismissed without a result
    val picker = handle.assertOpened<DatePicker>()
    picker.sendClosedForTest()
    // endregion
}

// region docs:testing/main-dispatcher
@BeforeTest
fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
}

@AfterTest
fun tearDown() {
    Dispatchers.resetMain()
}
// endregion
