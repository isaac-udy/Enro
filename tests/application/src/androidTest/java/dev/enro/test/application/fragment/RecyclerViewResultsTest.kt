package dev.enro.test.application.fragment

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.enro.test.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import dev.enro.tests.application.fragment.RecyclerViewItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecyclerViewResultsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun test() {
        // Navigate to RecyclerViewResults and test the Compose ViewHolder
        SelectDestinationRobot(composeRule)
            .openRecyclerViewResults()

            .assertItemAtPositionIs(0, RecyclerViewItem.Compose::class)
            .launchResultFor(0)
            .selectResultC()
            .assertResultAtPositionIs(0, "Even(C)")

            .assertItemAtPositionIs(1, RecyclerViewItem.ComposeWithRememberSaveableResult::class)
            .launchResultFor(1)
            .selectResultB()
            .assertResultAtPositionIs(1, "Odd(B)")

            .assertItemAtPositionIs(2, RecyclerViewItem.ComposeWithExternalResultChannel::class)
            .launchResultFor(2)
            .selectResultA()
            .assertResultAtPositionIs(2, "A")

            .assertItemAtPositionIs(3, RecyclerViewItem.ViewWithInternalResultChannel::class)
            .launchResultFor(3)
            .selectResultB()
            .assertResultAtPositionIs(3, "B")

            .assertItemAtPositionIs(4, RecyclerViewItem.ViewWithExternalResultChannel::class)
            .launchResultFor(4)
            .selectResultA()
            .assertResultAtPositionIs(4, "A")


            .assertItemAtPositionIs(100, RecyclerViewItem.Compose::class)
            .launchResultFor(100)
            .selectResultC()
            .assertResultAtPositionIs(100, "Even(C)")

            .assertItemAtPositionIs(101, RecyclerViewItem.ComposeWithRememberSaveableResult::class)
            .launchResultFor(101)
            .selectResultB()
            .assertResultAtPositionIs(101, "Odd(B)")

            .assertItemAtPositionIs(104, RecyclerViewItem.ViewWithExternalResultChannel::class)
            .launchResultFor(104)
            .selectResultA()
            .assertResultAtPositionIs(104, "A")

            .assertItemAtPositionIs(102, RecyclerViewItem.ComposeWithExternalResultChannel::class)
            .launchResultFor(102)
            .selectResultA()
            .assertResultAtPositionIs(102, "A")

            .assertItemAtPositionIs(103, RecyclerViewItem.ViewWithInternalResultChannel::class)
            .launchResultFor(103)
            .selectResultB()
            .assertResultAtPositionIs(103, "B")

            .assertItemAtPositionIs(5, RecyclerViewItem.Compose::class)
            .launchResultFor(5)
            .selectResultC()
            .assertResultAtPositionIs(5, "Odd(C)")


            .assertItemAtPositionIs(0, RecyclerViewItem.Compose::class)
            .launchResultFor(0)
            .selectResultA()
            .assertResultAtPositionIs(0, "Even(A)")

            // We are not asserting the result of the first item again, because RecyclerViews do not
            // play well with rememberSaveable in Compose
//            .assertResultAtPositionIs(1, "Odd(B)")
            .assertResultAtPositionIs(2, "A")
            .assertResultAtPositionIs(3, "B")
            .assertResultAtPositionIs(4, "A")


    }
}