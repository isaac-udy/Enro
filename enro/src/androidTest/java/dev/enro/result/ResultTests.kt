package dev.enro.result

import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import dev.enro.DefaultActivity
import dev.enro.DefaultActivityKey
import dev.enro.core.asTyped
import dev.enro.core.forward
import dev.enro.core.getNavigationHandle
import dev.enro.core.result.closeWithResult
import dev.enro.expectActivity
import dev.enro.expectContext
import junit.framework.Assert.*
import org.junit.Test
import java.util.*

class ResultTests {
    @Test
    fun whenActivityRequestsResult_andResultProviderIsStandaloneFragment_thenResultIsReceived() {
        val scenario = ActivityScenario.launch(ResultReceiverActivity::class.java)
        val result = UUID.randomUUID().toString()
        scenario.onActivity {
            it.resultChannel.open(FragmentResultKey())
        }

        expectContext<ResultFragment, FragmentResultKey>()
            .navigation
            .closeWithResult(result)

        val activity = expectActivity<ResultReceiverActivity>()

        assertEquals(result, activity.result)
    }

    @Test
    fun whenActivityRequestsResult_andResultProviderIsActivity_thenResultIsReceived() {
        val scenario = ActivityScenario.launch(ResultReceiverActivity::class.java)
        val result = UUID.randomUUID().toString()
        scenario.onActivity {
            it.resultChannel.open(ActivityResultKey())
        }

        val resultActivity = expectActivity<ResultActivity>()
        resultActivity.getNavigationHandle()
            .asTyped<ActivityResultKey>()
            .closeWithResult(result)

        val activity = expectActivity<ResultReceiverActivity>()

        assertEquals(result, activity.result)
    }

    @Test
    fun whenActivityRequestsResult_andResultProviderIsNestedFragment_thenResultIsReceived() {
        val scenario = ActivityScenario.launch(ResultReceiverActivity::class.java)
        val result = UUID.randomUUID().toString()
        scenario.onActivity {
            it.resultChannel.open(NestedResultFragmentKey())
        }

        expectContext<NestedResultFragment, NestedResultFragmentKey>()
            .navigation
            .closeWithResult(result)

        val activity = expectActivity<ResultReceiverActivity>()

        assertEquals(result, activity.result)
    }


    @Test
    fun whenActivityRequestsResultThroughMultipleChannels_andResultProviderIsFragment_thenChannelUniquenessIsPreserved() {
        ActivityScenario.launch(ResultReceiverActivity::class.java)
        val result = UUID.randomUUID().toString()
        val secondaryResult = UUID.randomUUID().toString()

        expectActivity<ResultReceiverActivity>()
            .resultChannel
            .open(FragmentResultKey())

        expectContext<ResultFragment, FragmentResultKey>()
            .navigation
            .closeWithResult(result)

        expectActivity<ResultReceiverActivity>()
            .secondaryResultChannel
            .open(FragmentResultKey())

        expectContext<ResultFragment, FragmentResultKey>()
            .navigation
            .closeWithResult(secondaryResult)

        val activity = expectActivity<ResultReceiverActivity>()

        assertEquals(result, activity.result)
        assertEquals(secondaryResult, activity.secondaryResult)
    }

    @Test
    fun whenActivityRequestsResultThroughMultipleChannels_andResultProviderIsActivity_thenChannelUniquenessIsPreserved() {
        ActivityScenario.launch(ResultReceiverActivity::class.java)
        val result = UUID.randomUUID().toString()
        val secondaryResult = UUID.randomUUID().toString()

        expectActivity<ResultReceiverActivity>()
            .resultChannel
            .open(ActivityResultKey())

        expectActivity<ResultActivity>()
            .getNavigationHandle()
            .asTyped<ActivityResultKey>()
            .closeWithResult(result)

        expectActivity<ResultReceiverActivity>()
            .secondaryResultChannel
            .open(ActivityResultKey())

        expectActivity<ResultActivity>()
            .getNavigationHandle()
            .asTyped<ActivityResultKey>()
            .closeWithResult(secondaryResult)

        val activity = expectActivity<ResultReceiverActivity>()

        assertEquals(result, activity.result)
        assertEquals(secondaryResult, activity.secondaryResult)
    }

    @Test
    fun whenActivityRequestsResult_andActivityIsReCreated_thenResultIsStillSent() {
        val scenario = ActivityScenario.launch(ResultReceiverActivity::class.java)
        val result = UUID.randomUUID().toString()

        val initialActivity = expectActivity<ResultReceiverActivity>()
        val initalActivityHash = initialActivity.hashCode()

        scenario.recreate()
            .onActivity {
                it.resultChannel
                    .open(ActivityResultKey())
            }

        expectContext<ResultActivity, ActivityResultKey>()
            .navigation
            .closeWithResult(result)

        val activity = expectActivity<ResultReceiverActivity>()

        assertEquals(result, activity.result)
        assertFalse(initalActivityHash == activity.hashCode())
    }

    @Test
    fun whenFragmentRequestsResult_andResultProviderIsStandaloneFragment_thenResultIsReceived() {
        val s =ActivityScenario.launch(DefaultActivity::class.java)
        val result = UUID.randomUUID().toString()

        expectContext<DefaultActivity, DefaultActivityKey>()
            .navigation
            .forward(ResultReceiverFragmentKey())

        val activity = expectActivity<FragmentActivity>()
        println(activity.toString())

        expectContext<ResultReceiverFragment, ResultReceiverFragmentKey>()
            .context
            .resultChannel
            .open(FragmentResultKey())

        val activity2 = expectActivity<FragmentActivity>()
        println(activity2.toString())

        expectContext<ResultFragment, FragmentResultKey>()
            .navigation
            .closeWithResult(result)

        val activity3 = expectActivity<FragmentActivity>()
        println(activity3.toString())
        assertEquals(
            result,
            expectContext<ResultReceiverFragment, ResultReceiverFragmentKey>()
                .context
                .result
        )
    }

    @Test
    fun whenFragmentRequestsResult_andResultProviderIsActivity_thenResultIsReceived() {
        ActivityScenario.launch(DefaultActivity::class.java)
        val result = UUID.randomUUID().toString()

        expectContext<DefaultActivity, DefaultActivityKey>()
            .navigation
            .forward(ResultReceiverFragmentKey())

        expectContext<ResultReceiverFragment, ResultReceiverFragmentKey>()
            .context
            .resultChannel
            .open(ActivityResultKey())

        expectContext<ResultActivity, ActivityResultKey>()
            .navigation
            .closeWithResult(result)

        assertEquals(
            result,
            expectContext<ResultReceiverFragment, ResultReceiverFragmentKey>()
                .context
                .result
        )
    }

    @Test
    fun whenFragmentRequestsResult_andResultProviderIsNestedFragment_thenResultIsReceived() {
        ActivityScenario.launch(DefaultActivity::class.java)
        val result = UUID.randomUUID().toString()

        expectContext<DefaultActivity, DefaultActivityKey>()
            .navigation
            .forward(NestedResultReceiverFragmentKey())

        expectContext<NestedResultReceiverFragment, NestedResultReceiverFragmentKey>()
            .context
            .resultChannel
            .open(NestedResultFragmentKey())

        expectContext<NestedResultFragment, NestedResultFragmentKey>()
            .navigation
            .closeWithResult(result)

        assertEquals(
            result,
            expectContext<NestedResultReceiverFragment, NestedResultReceiverFragmentKey>()
                .context
                .result
        )
    }

    @Test
    fun whenNestedFragmentRequestsResult_andResultProviderIsStandaloneFragment_thenResultIsReceived() {
        ActivityScenario.launch(NestedResultReceiverActivity::class.java)
        val result = UUID.randomUUID().toString()

        expectContext<NestedResultReceiverActivity, NestedResultReceiverActivityKey>()
            .navigation
            .forward(ResultReceiverFragmentKey())

        expectContext<ResultReceiverFragment, ResultReceiverFragmentKey>()
            .context
            .resultChannel
            .open(FragmentResultKey())

        expectContext<ResultFragment, FragmentResultKey>()
            .navigation
            .closeWithResult(result)

        assertEquals(
            result,
            expectContext<ResultReceiverFragment, ResultReceiverFragmentKey>()
                .context
                .result
        )
    }

    @Test
    fun whenNestedFragmentRequestsResult_andResultProviderIsActivity_thenResultIsReceived() {
        ActivityScenario.launch(NestedResultReceiverActivity::class.java)
        val result = UUID.randomUUID().toString()

        expectContext<NestedResultReceiverActivity, NestedResultReceiverActivityKey>()
            .navigation
            .forward(ResultReceiverFragmentKey())

        expectContext<ResultReceiverFragment, ResultReceiverFragmentKey>()
            .context
            .resultChannel
            .open(ActivityResultKey())

        expectContext<ResultActivity, ActivityResultKey>()
            .navigation
            .closeWithResult(result)

        assertEquals(
            result,
            expectContext<ResultReceiverFragment, ResultReceiverFragmentKey>()
                .context
                .result
        )
    }

    @Test
    fun whenNestedFragmentRequestsResult_andResultProviderIsNestedFragment_thenResultIsReceived() {
        ActivityScenario.launch(NestedResultReceiverActivity::class.java)
        val result = UUID.randomUUID().toString()

        expectContext<NestedResultReceiverActivity, NestedResultReceiverActivityKey>()
            .navigation
            .forward(ResultReceiverFragmentKey())

        expectContext<ResultReceiverFragment, ResultReceiverFragmentKey>()
            .context
            .resultChannel
            .open(NestedResultFragmentKey())

        expectContext<NestedResultFragment, NestedResultFragmentKey>()
            .navigation
            .closeWithResult(result)

        assertEquals(
            result,
            expectContext<ResultReceiverFragment, ResultReceiverFragmentKey>()
                .context
                .result
        )
    }

    @Test
    fun whenNestedFragmentRequestsResult_andResultProviderIsNestedFragmentSideBySideWithFragment_thenResultIsReceived() {
        ActivityScenario.launch(SideBySideNestedResultReceiverActivity::class.java)
        val result = UUID.randomUUID().toString()

        expectContext<SideBySideNestedResultReceiverActivity, SideBySideNestedResultReceiverActivityKey>()
            .navigation
            .forward(ResultReceiverFragmentKey())

        expectContext<ResultReceiverFragment, ResultReceiverFragmentKey>()
            .context
            .resultChannel
            .open(NestedResultFragmentKey())

        expectContext<NestedResultFragment, NestedResultFragmentKey>()
            .navigation
            .closeWithResult(result)

        assertEquals(
            result,
            expectContext<ResultReceiverFragment, ResultReceiverFragmentKey>()
                .context
                .result
        )
    }

    @Test
    fun whenActivityRequestResult_andResultProviderIsSyntheticDestination_andSyntheticDestinationSendsImmediateResult_thenResultIsReceived() {
        ActivityScenario.launch(ResultReceiverActivity::class.java)
        val expectedResult = UUID.randomUUID().toString()

        expectContext<ResultReceiverActivity, ResultReceiverActivityKey>()
            .context
            .resultChannel
            .open(
                ImmediateSyntheticResultKey(
                    reversedResult = expectedResult.reversed()
                )
            )

        assertEquals(
            expectedResult,
            expectContext<ResultReceiverActivity, ResultReceiverActivityKey>()
                .context
                .result
        )
    }

    @Test
    fun whenFragmentRequestResult_andResultProviderIsSyntheticDestination_andSyntheticDestinationSendsImmediateResult_thenResultIsReceived() {
        ActivityScenario.launch(DefaultActivity::class.java)
        val expectedResult = UUID.randomUUID().toString()

        expectContext<DefaultActivity, DefaultActivityKey>()
            .navigation
            .forward(ResultReceiverFragmentKey())

        expectContext<ResultReceiverFragment, ResultReceiverFragmentKey>()
            .context
            .resultChannel
            .open(
                ImmediateSyntheticResultKey(
                    reversedResult = expectedResult.reversed()
                )
            )

        assertEquals(
            expectedResult,
            expectContext<ResultReceiverFragment, ResultReceiverFragmentKey>()
                .context
                .result
        )
    }

    @Test
    fun whenSyntheticDestinationIsOpened_andSyntheticDestinationForwardsResultFromActivity_andSyntheticDestinationWasNotOpenedForResult_thenForwardedScreenIsStillOpened() {
        ActivityScenario.launch(ResultReceiverActivity::class.java)

        expectContext<ResultReceiverActivity, ResultReceiverActivityKey>()
            .navigation
            .forward(
                ForwardingSyntheticActivityResultKey()
            )

        expectContext<ResultActivity, ActivityResultKey>()
    }

    @Test
    fun whenSyntheticDestinationIsOpened_andSyntheticDestinationForwardsResultFromFragment_andSyntheticDestinationWasNotOpenedForResult_thenForwardedScreenIsStillOpened() {
        ActivityScenario.launch(ResultReceiverActivity::class.java)

        expectContext<ResultReceiverActivity, ResultReceiverActivityKey>()
            .navigation
            .forward(
                ForwardingSyntheticFragmentResultKey()
            )

        expectContext<ResultFragment, FragmentResultKey>()
    }


    @Test
    fun whenActivityRequestResult_andResultProviderIsSyntheticDestination_andSyntheticDestinationForwardsResultFromActivityKey_thenResultIsReceived() {
        ActivityScenario.launch(ResultReceiverActivity::class.java)
        val expectedResult = UUID.randomUUID().toString()

        expectContext<ResultReceiverActivity, ResultReceiverActivityKey>()
            .context
            .resultChannel
            .open(ForwardingSyntheticActivityResultKey())

        expectContext<ResultActivity, ActivityResultKey>()
            .navigation
            .closeWithResult(expectedResult)

        assertEquals(
            expectedResult,
            expectContext<ResultReceiverActivity, ResultReceiverActivityKey>()
                .context
                .result
        )
    }

    @Test
    fun whenFragmentRequestResult_andResultProviderIsSyntheticDestination_andSyntheticDestinationForwardsResultFromActivityKey_thenResultIsReceived() {
        ActivityScenario.launch(DefaultActivity::class.java)
        val expectedResult = UUID.randomUUID().toString()

        expectContext<DefaultActivity, DefaultActivityKey>()
            .navigation
            .forward(ResultReceiverFragmentKey())

        expectContext<ResultReceiverFragment, ResultReceiverFragmentKey>()
            .context
            .resultChannel
            .open(ForwardingSyntheticActivityResultKey())

        expectContext<ResultActivity, ActivityResultKey>()
            .navigation
            .closeWithResult(expectedResult)

        assertEquals(
            expectedResult,
            expectContext<ResultReceiverFragment, ResultReceiverFragmentKey>()
                .context
                .result
        )
    }

    @Test
    fun whenActivityRequestResult_andResultProviderIsSyntheticDestination_andSyntheticDestinationForwardsResultFromFragmentKey_thenResultIsReceived() {
        ActivityScenario.launch(ResultReceiverActivity::class.java)
        val expectedResult = UUID.randomUUID().toString()

        expectContext<ResultReceiverActivity, ResultReceiverActivityKey>()
            .context
            .resultChannel
            .open(ForwardingSyntheticFragmentResultKey())

        expectContext<ResultFragment, FragmentResultKey>()
            .navigation
            .closeWithResult(expectedResult)

        assertEquals(
            expectedResult,
            expectContext<ResultReceiverActivity, ResultReceiverActivityKey>()
                .context
                .result
        )
    }

    @Test
    fun whenFragmentRequestResult_andResultProviderIsSyntheticDestination_andSyntheticDestinationForwardsResultFromFragmentKey_thenResultIsReceived() {
        ActivityScenario.launch(DefaultActivity::class.java)
        val expectedResult = UUID.randomUUID().toString()

        expectContext<DefaultActivity, DefaultActivityKey>()
            .navigation
            .forward(ResultReceiverFragmentKey())

        expectContext<ResultReceiverFragment, ResultReceiverFragmentKey>()
            .context
            .resultChannel
            .open(ForwardingSyntheticFragmentResultKey())

        expectContext<ResultFragment, FragmentResultKey>()
            .navigation
            .closeWithResult(expectedResult)

        assertEquals(
            expectedResult,
            expectContext<ResultReceiverFragment, ResultReceiverFragmentKey>()
                .context
                .result
        )
    }

    @Test
    fun whenActivityRequestsResult_andResultOpensActivityThatUsesViewModelToForwardResult_thenResultIsForwarded() {
        ActivityScenario.launch(ResultReceiverActivity::class.java)
        val expectedResult = UUID.randomUUID().toString()

        expectContext<ResultReceiverActivity, ResultReceiverActivityKey>()
            .context
            .resultChannel
            .open(ViewModelForwardingResultActivityKey())

        expectContext<ResultActivity, ActivityResultKey>()
            .navigation
            .closeWithResult(expectedResult)

        assertEquals(
            expectedResult,
            expectContext<ResultReceiverActivity, ResultReceiverActivityKey>()
                .context
                .result
        )
    }

    @Test
    fun whenActivityRequestsResult_andResultOpensFragmentThatUsesViewModelToForwardResult_thenResultIsForwarded() {
        ActivityScenario.launch(ResultReceiverActivity::class.java)
        val expectedResult = UUID.randomUUID().toString()

        expectContext<ResultReceiverActivity, ResultReceiverActivityKey>()
            .context
            .resultChannel
            .open(ViewModelForwardingResultFragmentKey())

        expectContext<ResultActivity, ActivityResultKey>()
            .navigation
            .closeWithResult(expectedResult)

        assertEquals(
            expectedResult,
            expectContext<ResultReceiverActivity, ResultReceiverActivityKey>()
                .context
                .result
        )
    }

    @Test
    fun whenResultFlowActivityIsLaunched_thenStringRequestIsImmediatelyLaunched() {
        ActivityScenario.launch(DefaultActivity::class.java)
        expectContext<DefaultActivity, DefaultActivityKey>()
            .navigation
            .forward(ResultFlowKey())

        expectContext<ResultFlowActivity, ResultFlowKey>()
        expectContext<ResultFragment, FragmentResultKey>()
    }

    @Test
    fun whenResultFlowActivityIsLaunched_andFirstStringRequestIsClose_thenResultFlowActivityCloses() {
        ActivityScenario.launch(DefaultActivity::class.java)
        expectContext<DefaultActivity, DefaultActivityKey>()
            .navigation
            .forward(ResultFlowKey())

        expectContext<ResultFlowActivity, ResultFlowKey>()
        expectContext<ResultFragment, FragmentResultKey>()
            .navigation
            .closeWithResult("close")

        expectContext<DefaultActivity, DefaultActivityKey>()
    }

    @Test
    fun whenResultFlowActivityIsLaunched_andFirstStringRequestProceeds_thenAnotherStringRequestIsLaunched() {
        ActivityScenario.launch(DefaultActivity::class.java)
        expectContext<DefaultActivity, DefaultActivityKey>()
            .navigation
            .forward(ResultFlowKey())

        val activityFlow = expectContext<ResultFlowActivity, ResultFlowKey>()
        val firstRequest = expectContext<ResultFragment, FragmentResultKey>()
        firstRequest
            .navigation
            .closeWithResult("next")

        activityFlow.context.supportFragmentManager.hashCode()
        val secondRequest = expectContext<ResultFragment, FragmentResultKey>()
        assertNotSame(firstRequest.navigation.id, secondRequest.navigation.id)
    }

    @Test
    fun whenResultFlowIsLaunchedInDialogFragment_andCompletesThroughTwoNestedFragments_thenResultIsDelivered() {
        ActivityScenario.launch(DefaultActivity::class.java)
        expectContext<DefaultActivity, DefaultActivityKey>()
            .navigation
            .forward(ResultFlowDialogFragmentRootKey())

        // This is not a good solution, but the crash that this test detects happens due to an async
        // action causing a bad fragment removal, so we need to give the test time to detect the
        // crash before we consider the test successful
        Thread.sleep(1000)

        val root = expectContext<ResultFlowFragmentRootActivity, ResultFlowDialogFragmentRootKey>()
            .context
        assertEquals("******", root.lastResult)
    }
}