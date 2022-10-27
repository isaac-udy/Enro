package dev.enro.core.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import dev.enro.TestFragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.core.controller.interceptor.builder.InterceptorBehavior
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import dev.enro.core.destinations.*
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.expectFragmentContext
import dev.enro.expectNoFragmentContext
import dev.enro.waitFor
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.junit.After
import org.junit.Before
import org.junit.Test

class FragmentContainerInterceptor {

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
                InterceptorBehavior.Cancel
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
                InterceptorBehavior.Continue
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
                InterceptorBehavior.ReplaceWith(
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
                InterceptorBehavior.ReplaceWith(
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
                InterceptorBehavior.Cancel
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
    fun givenFragmentContainer_whenInterceptorAllowsClose_thenInterceptorIsCalled_andChildIsClosed() {
        var interceptorCalled = false
        interceptor = {
            onClosed<FragmentDestinations.PushesToPrimary> {
                interceptorCalled = true
                InterceptorBehavior.Continue
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
                InterceptorBehavior.ReplaceWith(
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
                InterceptorBehavior.ReplaceWith(
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
                InterceptorBehavior.ReplaceWith(
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
object FragmentScreenWithContainerInterceptor: NavigationKey.SupportsPresent

@NavigationDestination(FragmentScreenWithContainerInterceptor::class)
class FragmentWithContainerInterceptor : Fragment() {
    val container by navigationContainer(
        containerId = TestFragment.primaryFragmentContainer,
        interceptor = FragmentContainerInterceptor.interceptor,
    )

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