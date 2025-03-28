package dev.enro.core.fragment

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import dev.enro.TestFragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import dev.enro.core.destinations.*
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.result.registerForNavigationResult
import dev.enro.expectFragmentContext
import dev.enro.expectNoFragmentContext
import dev.enro.waitFor
import junit.framework.TestCase
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

class FragmentContainerInterceptor {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Before
    fun before() {
        interceptor = {}
    }

    @After
    fun after() {
        interceptor = {}
    }

    companion object {
        @IgnoredOnParcel
        var interceptor: NavigationInterceptorBuilder.() -> Unit = {}
    }

    @Test
    fun givenFragmentContainer_whenInterceptorPreventsOpeningChildren_andChildIsAttemptedToOpen_thenNothingIsOpened_andInterceptorIsCalled() {
        var interceptorCalled = false
        interceptor = {
            onOpen<FragmentDestinations.PushesToPrimary> {
                interceptorCalled = true
                cancelNavigation()
            }
        }
        val context = launchFragment(FragmentScreenWithContainerInterceptor)
        // We're pushing on to the parent container here, so this instruction should go through
        val primary = context.assertPushesTo<Fragment, FragmentDestinations.PushesToPrimary>(IntoChildContainer)

        // But once we attempt to execute an instruction on a context that inside of the container with the interceptor,
        // we should hit the interceptor and not open this instruction
        primary.navigation.push(FragmentDestinations.PushesToPrimary("WONT_OPEN"))

        waitFor {
            interceptorCalled
        }
        expectNoFragmentContext<FragmentDestinations.PushesToPrimary> {
            it.navigation.key.id == "WONT_OPEN"
        }
    }

    @Test
    fun givenFragmentContainer_whenInterceptorAllowsOpeningChildren_andChildIsAttemptedToOpen_thenInterceptorIsCalled_andChildOpens() {
        var interceptorCalled = false
        interceptor = {
            onOpen<FragmentDestinations.PushesToPrimary> {
                interceptorCalled = true
                continueWithNavigation()
            }
        }
        val context = launchFragment(FragmentScreenWithContainerInterceptor)
        // We're pushing on to the parent container here, so this instruction should go through
        val primary = context.assertPushesTo<Fragment, FragmentDestinations.PushesToPrimary>(IntoChildContainer)

        // But once we attempt to execute an instruction on a context that inside of the container with the interceptor,
        // we should hit the interceptor and not open this instruction
        primary.navigation.push(FragmentDestinations.PushesToPrimary("WILL_OPEN"))

        waitFor {
            interceptorCalled
        }
        expectFragmentContext<FragmentDestinations.PushesToPrimary> {
            it.navigation.key.id == "WILL_OPEN"
        }
    }

    @Test
    fun givenFragmentContainer_whenInterceptorReplacesInstruction_andChildIsAttemptedToOpen_thenInterceptorIsCalled_andChildIsReplaced_push() {
        var interceptorCalled = false
        interceptor = {
            onOpen<FragmentDestinations.PushesToPrimary> {
                interceptorCalled = true
                replaceNavigationWith(
                    NavigationInstruction.Push(FragmentDestinations.PushesToPrimary("REPLACED"))
                )
            }
        }
        val context = launchFragment(FragmentScreenWithContainerInterceptor)
        // We're pushing on to the parent container here, so this instruction should go through
        val primary = context.assertPushesTo<Fragment, FragmentDestinations.PushesToPrimary>(IntoChildContainer)

        // But once we attempt to execute an instruction on a context that inside of the container with the interceptor,
        // we should hit the interceptor and not open this instruction
        primary.navigation.push(FragmentDestinations.PushesToPrimary("NEVER_OPENED"))

        waitFor {
            interceptorCalled
        }
        expectFragmentContext<FragmentDestinations.PushesToPrimary> {
            it.navigation.key.id == "REPLACED"
        }
    }

    @Test
    fun givenFragmentContainer_whenInterceptorReplacesInstruction_andChildIsAttemptedToOpen_thenInterceptorIsCalled_andChildIsReplaced_present() {
        var interceptorCalled = false
        interceptor = {
            onOpen<FragmentDestinations.PushesToPrimary> {
                interceptorCalled = true
                replaceNavigationWith(
                    NavigationInstruction.Present(FragmentDestinations.Presentable("REPLACED"))
                )
            }
        }
        val context = launchFragment(FragmentScreenWithContainerInterceptor)
        // We're pushing on to the parent container here, so this instruction should go through
        val primary = context.assertPushesTo<Fragment, FragmentDestinations.PushesToPrimary>(IntoChildContainer)

        // But once we attempt to execute an instruction on a context that inside of the container with the interceptor,
        // we should hit the interceptor and not open this instruction
        primary.navigation.push(FragmentDestinations.PushesToPrimary("NEVER_OPENED"))

        waitFor {
            interceptorCalled
        }
        expectFragmentContext<FragmentDestinations.Presentable> {
            it.navigation.key.id == "REPLACED"
        }
    }

    @Test
    fun givenFragmentContainer_whenInterceptorPreventsClose_thenInterceptorIsCalled_andChildIsNotClosed() {
        var interceptorCalled = false
        interceptor = {
            onClosed<FragmentDestinations.PushesToPrimary> {
                interceptorCalled = true
                cancelClose()
            }
        }

        val expectedKey = FragmentDestinations.PushesToPrimary("STAYS_OPEN")
        launchFragment(FragmentScreenWithContainerInterceptor)
            .assertPushesTo<Fragment, FragmentDestinations.PushesToPrimary>(IntoChildContainer, expectedKey)
            .navigation
            .close()

        waitFor {
            interceptorCalled
        }
        expectFragmentContext<FragmentDestinations.PushesToPrimary> { it.navigation.key == expectedKey }
    }

    @Test
    fun givenFragmentContainer_whenInterceptorPreventsCloseButDeliversResult_thenInterceptorIsCalled_andChildIsNotClosed_andResultIsDelivered() {
        var interceptorCalled = false
        var resultDelivered: Any? = null
        interceptor = {
            onResult<FragmentDestinations.PushesToPrimary, TestResult> { _, result ->
                interceptorCalled = true
                resultDelivered = result
                deliverResultAndCancelClose()
            }
        }

        val expectedResult = TestResult(UUID.randomUUID().toString())
        val expectedKey = FragmentDestinations.PushesToPrimary("STAYS_OPEN")
        val containerContext = launchFragment(FragmentScreenWithContainerInterceptor)

        (containerContext.context as FragmentWithContainerInterceptor)
            .resultChannel
            .push(expectedKey)

        expectFragmentContext<FragmentDestinations.PushesToPrimary> { it.navigation.key == expectedKey }
            .navigation
            .closeWithResult(expectedResult)

        waitFor {
            interceptorCalled
        }
        expectFragmentContext<FragmentDestinations.PushesToPrimary> { it.navigation.key == expectedKey }
        TestCase.assertEquals(expectedResult, resultDelivered)
        TestCase.assertEquals(expectedResult, containerContext.context.lastResult)
    }

    @Test
    fun givenFragmentContainer_whenInterceptorAllowsClose_thenInterceptorIsCalled_andChildIsClosed() {
        var interceptorCalled = false
        interceptor = {
            onClosed<FragmentDestinations.PushesToPrimary> {
                interceptorCalled = true
                continueWithClose()
            }
        }

        val shouldBeClosed = FragmentDestinations.PushesToPrimary("IS_CLOSED")
        launchFragment(FragmentScreenWithContainerInterceptor)
            .assertPushesTo<Fragment, FragmentDestinations.PushesToPrimary>(IntoChildContainer, shouldBeClosed)
            .navigation
            .close()


        waitFor {
            interceptorCalled
        }
        expectNoFragmentContext<FragmentDestinations.PushesToPrimary> { it.navigation.key == shouldBeClosed }
    }

    @Test
    fun givenFragmentContainer_whenInterceptorReplacesCloseInstruction_thenInterceptorIsCalled_andChildIsReplaced_push() {
        var interceptorCalled = false
        interceptor = {
            onClosed<FragmentDestinations.PushesToPrimary> {
                interceptorCalled = true
                replaceCloseWith(
                    NavigationInstruction.Push(FragmentDestinations.PushesToPrimary("REPLACED"))
                )
            }
        }

        val shouldBeClosed = FragmentDestinations.PushesToPrimary("IS_CLOSED")
        launchFragment(FragmentScreenWithContainerInterceptor)
            .assertPushesTo<Fragment, FragmentDestinations.PushesToPrimary>(IntoChildContainer, shouldBeClosed)
            .navigation
            .close()


        waitFor {
            interceptorCalled
        }
        expectFragmentContext<FragmentDestinations.PushesToPrimary> { it.navigation.key.id == "REPLACED" }
    }

    @Test
    fun givenFragmentContainer_whenInterceptorReplacesCloseInstruction_thenInterceptorIsCalled_andChildIsReplaced_present() {
        var interceptorCalled = false
        interceptor = {
            onClosed<FragmentDestinations.PushesToPrimary> {
                interceptorCalled = true
                replaceCloseWith(
                    NavigationInstruction.Present(FragmentDestinations.Presentable("REPLACED"))
                )
            }
        }

        val shouldBeClosed = FragmentDestinations.PushesToPrimary("IS_CLOSED")
        launchFragment(FragmentScreenWithContainerInterceptor)
            .assertPushesTo<Fragment, FragmentDestinations.PushesToPrimary>(IntoChildContainer, shouldBeClosed)
            .navigation
            .close()

        waitFor {
            interceptorCalled
        }
        expectFragmentContext<FragmentDestinations.Presentable> { it.navigation.key.id == "REPLACED" }
    }

    @Test
    fun givenFragmentContainer_whenInterceptorInterceptsResult_thenInterceptorIsCalled() {
        var interceptorCalled = false
        interceptor = {
            onResult<FragmentDestinations.PushesToPrimary, TestResult> { key, result ->
                interceptorCalled = true
                replaceCloseWith(
                    NavigationInstruction.Push(FragmentDestinations.PushesToPrimary("REPLACED_ACTION"))
                )
            }
        }

        val initialKey = FragmentDestinations.PushesToPrimary("INITIAL_KEY")
        launchFragment(FragmentScreenWithContainerInterceptor)
            .assertPushesTo<Fragment, FragmentDestinations.PushesToPrimary>(IntoChildContainer, initialKey)
            .navigation
            .closeWithResult(TestResult("REPLACED_ACTION"))

        waitFor {
            interceptorCalled
        }
        expectFragmentContext<FragmentDestinations.PushesToPrimary> { it.navigation.key.id == "REPLACED_ACTION" }
            .navigation
            .close()

        expectFragmentContext<FragmentDestinations.PushesToPrimary> { it.navigation.key == initialKey }
    }
}

@Parcelize
object FragmentScreenWithContainerInterceptor: Parcelable, NavigationKey.SupportsPresent

@NavigationDestination(FragmentScreenWithContainerInterceptor::class)
class FragmentWithContainerInterceptor : Fragment() {
    val container by navigationContainer(
        containerId = TestFragment.primaryFragmentContainer,
        interceptor = FragmentContainerInterceptor.interceptor,
    )

    var lastResult: TestResult? = null
    val resultChannel by registerForNavigationResult<TestResult> {
        lastResult = it
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FrameLayout(requireContext()).apply {
            id = TestFragment.primaryFragmentContainer
        }
    }
}