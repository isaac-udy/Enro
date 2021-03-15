package dev.enro.result

import androidx.test.core.app.ActivityScenario
import dev.enro.expectActivity
import dev.enro.expectFragment
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.util.*

class ResultForwardingTests {

    @Test
    fun whenActivityRequestsResultFromFlow_andFlowCompletesSuccessfully_thenActivityReceivesFullFlowResult() {
        val expectedResult = FlowTestResult(
                firstData = UUID.randomUUID().toString(),
                secondData = UUID.randomUUID().toString(),
                thirdData = UUID.randomUUID().toString()
        )
        val scenario = ActivityScenario.launch(ResultReceiverActivity::class.java)
        scenario.onActivity {
            it.flowResultChannel.open(FlowTestPartOne())
        }

        expectFragment<FlowTestPartOneFragment>()
                .next(expectedResult.firstData)

        expectFragment<FlowTestPartTwoFragment>()
                .next(expectedResult.secondData)

        expectFragment<FlowTestPartThreeFragment>()
                .finishFlow(expectedResult.thirdData)

        val result = expectActivity<ResultReceiverActivity>().flowResult

        assertEquals(expectedResult, result)
    }
}