package dev.enro.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.core.controller.interceptor.builder.InterceptorBehavior
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import dev.enro.core.destinations.*
import dev.enro.expectComposableContext
import dev.enro.expectNoComposableContext
import dev.enro.waitFor
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.junit.After
import org.junit.Before
import org.junit.Test

class ComposeContainerInterceptor {

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
                InterceptorBehavior.Cancel
            }
        }
        val context = launchComposable(ComposeScreenWithContainerInterceptor)
        // We're pushing on to the parent container here, so this instruction should go through
        val primary = context.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer)

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
                InterceptorBehavior.Continue
            }
        }
        val context = launchComposable(ComposeScreenWithContainerInterceptor)
        // We're pushing on to the parent container here, so this instruction should go through
        val primary = context.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer)

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
                InterceptorBehavior.ReplaceWith(
                    NavigationInstruction.Push(ComposableDestinations.PushesToPrimary("REPLACED"))
                )
            }
        }
        val context = launchComposable(ComposeScreenWithContainerInterceptor)
        // We're pushing on to the parent container here, so this instruction should go through
        val primary = context.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer)

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
                InterceptorBehavior.ReplaceWith(
                    NavigationInstruction.Present(ComposableDestinations.Presentable("REPLACED"))
                )
            }
        }
        val context = launchComposable(ComposeScreenWithContainerInterceptor)
        // We're pushing on to the parent container here, so this instruction should go through
        val primary = context.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer)

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
                InterceptorBehavior.Cancel
            }
        }

        val expectedKey = ComposableDestinations.PushesToPrimary("STAYS_OPEN")
        launchComposable(ComposeScreenWithContainerInterceptor)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer, expectedKey)
            .navigation
            .close()

        waitFor {
            interceptorCalled
        }
        expectComposableContext<ComposableDestinations.PushesToPrimary> { it.navigation.key == expectedKey }
    }

    @Test
    fun givenComposeContainer_whenInterceptorAllowsClose_thenInterceptorIsCalled_andChildIsClosed() {
        var interceptorCalled = false
        interceptor = {
            onClosed<ComposableDestinations.PushesToPrimary> {
                interceptorCalled = true
                InterceptorBehavior.Continue
            }
        }

        val shouldBeClosed = ComposableDestinations.PushesToPrimary("IS_CLOSED")
        launchComposable(ComposeScreenWithContainerInterceptor)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer, shouldBeClosed)
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
                InterceptorBehavior.ReplaceWith(
                    NavigationInstruction.Push(ComposableDestinations.PushesToPrimary("REPLACED"))
                )
            }
        }

        val shouldBeClosed = ComposableDestinations.PushesToPrimary("IS_CLOSED")
        launchComposable(ComposeScreenWithContainerInterceptor)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer, shouldBeClosed)
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
                InterceptorBehavior.ReplaceWith(
                    NavigationInstruction.Present(ComposableDestinations.Presentable("REPLACED"))
                )
            }
        }

        val shouldBeClosed = ComposableDestinations.PushesToPrimary("IS_CLOSED")
        launchComposable(ComposeScreenWithContainerInterceptor)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer, shouldBeClosed)
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
                InterceptorBehavior.ReplaceWith(
                    NavigationInstruction.Push(ComposableDestinations.PushesToPrimary("REPLACED_ACTION"))
                )
            }
        }

        val initialKey = ComposableDestinations.PushesToPrimary("INITIAL_KEY")
        launchComposable(ComposeScreenWithContainerInterceptor)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer, initialKey)
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
object ComposeScreenWithContainerInterceptor: NavigationKey.SupportsPresent

@Composable
@NavigationDestination(ComposeScreenWithContainerInterceptor::class)
fun ContainerInterceptorScreen() {
    val navigation = navigationHandle<ComposeScreenWithContainerInterceptor>()
    val container = rememberNavigationContainer(
        interceptor = ComposeContainerInterceptor.interceptor
    )
    Box(modifier = Modifier.fillMaxWidth()) {
        container.Render()
    }
}