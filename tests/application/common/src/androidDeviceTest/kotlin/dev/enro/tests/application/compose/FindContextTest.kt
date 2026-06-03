package dev.enro.tests.application.compose

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.enro.tests.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import dev.enro.tests.application.compose.FindContext
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FindContextTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun testFindContext() {
        SelectDestinationRobot(composeRule)
            .openFindContext()
            .pushLeftTop()
            .pushRightTop()
            .pushLeftBottom()
            .pushRightBottom()
            .find()

            .setLeftTopTarget(1)
            .findContext()
            .assertContextNotFound()

            .setRightTopTarget(2)
            .findContext()
            .assertContextFound(FindContext.Right.Top(2))

            .setRightBottomTarget(null)
            .findContext()
            .assertContextFound(FindContext.Right.Bottom(2))
            .close()

            .pushLeftBottom()
            .find()
            .setLeftBottomTarget(3)
            .findContext()
            .assertContextFound(FindContext.Left.Bottom(3))

            .setLeftBottomTarget(2)
            .findContext()
            .assertContextNotFound()
    }

    @Test
    fun testFindActiveContext() {
        SelectDestinationRobot(composeRule)
            .openFindContext()
            .pushLeftTop()
            .pushRightTop()
            .pushLeftBottom()
            .pushRightBottom()
            .find()

            .setRightBottomTarget(1)
            .findActiveContext()
            .assertContextNotFound()

            .setRightBottomTarget(2)
            .findActiveContext()
            .assertContextFound(FindContext.Right.Bottom(2))

            .setRightTopTarget(2)
            .findActiveContext()
            .assertContextNotFound()

            .setLeftTopTarget(2)
            .findActiveContext()
            .assertContextNotFound()

            .setLeftBottomTarget(2)
            .findActiveContext()
            .assertContextNotFound()

            .close()
            .setActiveLeftTop()
            .find()

            .setLeftTopTarget(2)
            .findActiveContext()
            .assertContextFound(FindContext.Left.Top(2))

            .setLeftTopTarget(null)
            .findActiveContext()
            .assertContextFound(FindContext.Left.Top(2))

            .setLeftBottomTarget(null)
            .findActiveContext()
            .assertContextNotFound()
            .close()

            .pushLeftBottom()
            .find()

            .setLeftBottomTarget(null)
            .findActiveContext()
            .assertContextFound(FindContext.Left.Bottom(3))
    }

}