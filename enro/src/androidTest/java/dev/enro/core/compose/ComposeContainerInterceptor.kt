package dev.enro.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.closeWithResult
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import dev.enro.core.destinations.ComposableDestinations
import dev.enro.core.destinations.IntoChildContainer
import dev.enro.core.destinations.TestResult
import dev.enro.core.destinations.assertPushesTo
import dev.enro.core.destinations.launchComposable
import dev.enro.core.push
import dev.enro.core.result.registerForNavigationResult
import dev.enro.expectComposableContext
import dev.enro.expectNoComposableContext
import dev.enro.waitFor
import junit.framework.TestCase.assertEquals
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class ComposeContainerInterceptor {

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
    fun givenComposeContainer_whenInterceptorPreventsOpeningChildren_andChildIsAttemptedToOpen_thenNothingIsOpened_andInterceptorIsCalled() {
        var interceptorCalled = false
        interceptor = {
            onOpen<ComposableDestinations.PushesToPrimary> {
                interceptorCalled = true
                cancelNavigation()
            }
        }
        val context = launchComposable(ComposeScreenWithContainerInterceptor)
        // We're pushing on to the parent container here, so this instruction should go through
        val primary =
            context.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
                IntoChildContainer
            )

        // But once we attempt to execute an instruction on a context that inside of the container with the interceptor,
        // we should hit the interceptor and not open this instruction
        primary.navigation.push(ComposableDestinations.PushesToPrimary("WONT_OPEN"))

        waitFor {
            interceptorCalled
        }
        expectNoComposableContext<ComposableDestinations.PushesToPrimary> {
            it.navigation.key.id == "WONT_OPEN"
        }
    }

    @Test
    fun givenComposeContainer_whenInterceptorAllowsOpeningChildren_andChildIsAttemptedToOpen_thenInterceptorIsCalled_andChildOpens() {
        var interceptorCalled = false
        interceptor = {
            onOpen<ComposableDestinations.PushesToPrimary> {
                interceptorCalled = true
                continueWithNavigation()
            }
        }
        val context = launchComposable(ComposeScreenWithContainerInterceptor)
        // We're pushing on to the parent container here, so this instruction should go through
        val primary =
            context.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
                IntoChildContainer
            )

        // But once we attempt to execute an instruction on a context that inside of the container with the interceptor,
        // we should hit the interceptor and not open this instruction
        primary.navigation.push(ComposableDestinations.PushesToPrimary("WILL_OPEN"))

        waitFor {
            interceptorCalled
        }
        expectComposableContext<ComposableDestinations.PushesToPrimary> {
            it.navigation.key.id == "WILL_OPEN"
        }
    }

    @Test
    fun givenComposeContainer_whenInterceptorReplacesInstruction_andChildIsAttemptedToOpen_thenInterceptorIsCalled_andChildIsReplaced_push() {
        var interceptorCalled = false
        interceptor = {
            onOpen<ComposableDestinations.PushesToPrimary> {
                interceptorCalled = true
                replaceNavigationWith(
                    NavigationInstruction.Push(ComposableDestinations.PushesToPrimary("REPLACED"))
                )
            }
        }
        val context = launchComposable(ComposeScreenWithContainerInterceptor)
        // We're pushing on to the parent container here, so this instruction should go through
        val primary =
            context.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
                IntoChildContainer
            )

        // But once we attempt to execute an instruction on a context that inside of the container with the interceptor,
        // we should hit the interceptor and not open this instruction
        primary.navigation.push(ComposableDestinations.PushesToPrimary("NEVER_OPENED"))

        waitFor {
            interceptorCalled
        }
        expectComposableContext<ComposableDestinations.PushesToPrimary> {
            it.navigation.key.id == "REPLACED"
        }
    }

    @Test
    fun givenComposeContainer_whenInterceptorReplacesInstruction_andChildIsAttemptedToOpen_thenInterceptorIsCalled_andChildIsReplaced_present() {
        var interceptorCalled = false
        interceptor = {
            onOpen<ComposableDestinations.PushesToPrimary> {
                interceptorCalled = true
                replaceNavigationWith(
                    NavigationInstruction.Present(ComposableDestinations.Presentable("REPLACED"))
                )
            }
        }
        val context = launchComposable(ComposeScreenWithContainerInterceptor)
        // We're pushing on to the parent container here, so this instruction should go through
        val primary =
            context.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
                IntoChildContainer
            )

        // But once we attempt to execute an instruction on a context that inside of the container with the interceptor,
        // we should hit the interceptor and not open this instruction
        primary.navigation.push(ComposableDestinations.PushesToPrimary("NEVER_OPENED"))

        waitFor {
            interceptorCalled
        }
        expectComposableContext<ComposableDestinations.Presentable> {
            it.navigation.key.id == "REPLACED"
        }
    }

    @Test
    fun givenComposeContainer_whenInterceptorPreventsClose_thenInterceptorIsCalled_andChildIsNotClosed() {
        var interceptorCalled = false
        interceptor = {
            onClosed<ComposableDestinations.PushesToPrimary> {
                interceptorCalled = true
                cancelClose()
            }
        }

        val expectedKey = ComposableDestinations.PushesToPrimary("STAYS_OPEN")
        launchComposable(ComposeScreenWithContainerInterceptor)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
                IntoChildContainer,
                expectedKey
            )
            .navigation
            .close()

        waitFor {
            interceptorCalled
        }
        expectComposableContext<ComposableDestinations.PushesToPrimary> { it.navigation.key == expectedKey }
    }

    @Test
    fun givenComposeContainer_whenInterceptorPreventsCloseButDeliversResult_thenInterceptorIsCalled_andChildIsNotClosed_andResultIsDelivered() {
        var interceptorCalled = false
        var resultDelivered: Any? = null
        interceptor = {
            onResult<ComposableDestinations.PushesToPrimary, TestResult> { _, result ->
                interceptorCalled = true
                resultDelivered = result
                deliverResultAndCancelClose()
            }
        }

        val expectedResult = TestResult(UUID.randomUUID().toString())
        val expectedKey = ComposableDestinations.PushesToPrimary("STAYS_OPEN")
        val containerContext = launchComposable(ComposeScreenWithContainerInterceptor)
        val viewModel =
            ViewModelProvider(containerContext.navigationContext.viewModelStoreOwner)[ContainerInterceptorViewModel::class.java]

        viewModel.getResult.push(expectedKey)
        expectComposableContext<ComposableDestinations.PushesToPrimary> { it.navigation.key == expectedKey }
            .navigation
            .closeWithResult(expectedResult)

        waitFor {
            interceptorCalled
        }
        expectComposableContext<ComposableDestinations.PushesToPrimary> { it.navigation.key == expectedKey }
        assertEquals(expectedResult, resultDelivered)
        assertEquals(expectedResult, viewModel.lastResult)
    }

    @Test
    fun givenComposeContainer_whenInterceptorAllowsClose_thenInterceptorIsCalled_andChildIsClosed() {
        var interceptorCalled = false
        interceptor = {
            onClosed<ComposableDestinations.PushesToPrimary> {
                interceptorCalled = true
                continueWithClose()
            }
        }

        val shouldBeClosed = ComposableDestinations.PushesToPrimary("IS_CLOSED")
        launchComposable(ComposeScreenWithContainerInterceptor)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
                IntoChildContainer,
                shouldBeClosed
            )
            .navigation
            .close()


        waitFor {
            interceptorCalled
        }
        expectNoComposableContext<ComposableDestinations.PushesToPrimary> { it.navigation.key == shouldBeClosed }
    }

    @Test
    fun givenComposeContainer_whenInterceptorReplacesCloseInstruction_thenInterceptorIsCalled_andChildIsReplaced_push() {
        var interceptorCalled = false
        interceptor = {
            onClosed<ComposableDestinations.PushesToPrimary> {
                interceptorCalled = true
                replaceCloseWith(
                    NavigationInstruction.Push(ComposableDestinations.PushesToPrimary("REPLACED"))
                )
            }
        }

        val shouldBeClosed = ComposableDestinations.PushesToPrimary("IS_CLOSED")
        launchComposable(ComposeScreenWithContainerInterceptor)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
                IntoChildContainer,
                shouldBeClosed
            )
            .navigation
            .close()


        waitFor {
            interceptorCalled
        }
        expectComposableContext<ComposableDestinations.PushesToPrimary> { it.navigation.key.id == "REPLACED" }
    }

    @Test
    fun givenComposeContainer_whenInterceptorReplacesCloseInstruction_thenInterceptorIsCalled_andChildIsReplaced_present() {
        var interceptorCalled = false
        interceptor = {
            onClosed<ComposableDestinations.PushesToPrimary> {
                interceptorCalled = true
                replaceCloseWith(
                    NavigationInstruction.Present(ComposableDestinations.Presentable("REPLACED"))
                )
            }
        }

        val shouldBeClosed = ComposableDestinations.PushesToPrimary("IS_CLOSED")
        launchComposable(ComposeScreenWithContainerInterceptor)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
                IntoChildContainer,
                shouldBeClosed
            )
            .navigation
            .close()

        waitFor {
            interceptorCalled
        }
        expectComposableContext<ComposableDestinations.Presentable> { it.navigation.key.id == "REPLACED" }
    }

    @Test
    fun givenComposeContainer_whenInterceptorInterceptsResult_thenInterceptorIsCalled() {
        var interceptorCalled = false
        interceptor = {
            onResult<ComposableDestinations.PushesToPrimary, TestResult> { key, result ->
                interceptorCalled = true
                replaceCloseWith(
                    NavigationInstruction.Push(ComposableDestinations.PushesToPrimary("REPLACED_ACTION"))
                )
            }
        }

        val initialKey = ComposableDestinations.PushesToPrimary("INITIAL_KEY")
        launchComposable(ComposeScreenWithContainerInterceptor)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
                IntoChildContainer,
                initialKey
            )
            .navigation
            .closeWithResult(TestResult("REPLACED_ACTION"))

        waitFor {
            interceptorCalled
        }
        expectComposableContext<ComposableDestinations.PushesToPrimary> { it.navigation.key.id == "REPLACED_ACTION" }
            .navigation
            .close()

        expectComposableContext<ComposableDestinations.PushesToPrimary> { it.navigation.key == initialKey }
    }
}

@Parcelize
object ComposeScreenWithContainerInterceptor : NavigationKey.SupportsPresent

@Composable
@NavigationDestination(ComposeScreenWithContainerInterceptor::class)
fun ContainerInterceptorScreen() {
    val viewModel = viewModel<ContainerInterceptorViewModel>()
    val navigation = navigationHandle<ComposeScreenWithContainerInterceptor>()
    val container = rememberNavigationContainer(
        interceptor = ComposeContainerInterceptor.interceptor,
        emptyBehavior = EmptyBehavior.AllowEmpty,
    )
    Box(modifier = Modifier.fillMaxWidth()) {
        container.Render()
    }
}

class ContainerInterceptorViewModel() : ViewModel() {
    var lastResult: TestResult? = null
    val getResult by registerForNavigationResult<TestResult> {
        lastResult = it
    }
}