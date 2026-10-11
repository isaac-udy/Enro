package dev.enro.result.flow

import dev.enro.NavigationKey
import dev.enro.test.fixtures.NavigationContainerFixtures
import dev.enro.test.putNavigationHandleForViewModel
import dev.enro.test.runEnroTest
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

// Flow step ids are persisted in saved state, so the JVM format must not change.
class FlowStepIdFormatTest {

    @Test
    fun anonymousStepIds_useTheCallSiteClass() = runEnroTest {
        putNavigationHandleForViewModel<ResultFlowTest.ResultFlowViewModel, ResultFlowTest.ResultFlowDestination>(
            ResultFlowTest.ResultFlowDestination()
        )
        val viewModel = ResultFlowTest.ResultFlowViewModel()
        val container = NavigationContainerFixtures.createForFlow(viewModel.flow)

        val id = requireNotNull(container.backstack[0].flowStepId).value
        assertTrue(id.startsWith("class dev.enro.result.flow.ResultFlowTest\$ResultFlowViewModel"), id)
        assertTrue(id.endsWith("@0"), id)
    }

    @Test
    fun flowStepId_usesTheCallSiteClass() {
        fun firstCallSite() = flowStepId<NavigationKey>().value
        fun secondCallSite() = flowStepId<NavigationKey>().value

        assertTrue(firstCallSite().startsWith("dev.enro.result.flow.FlowStepIdFormatTest"), firstCallSite())
        assertNotEquals(firstCallSite(), secondCallSite())
    }
}
