package dev.enro.core.destinations

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import dev.enro.*
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.*
import dev.enro.destination.compose.ComposableDestination
import dev.enro.core.container.NavigationContainer
import dev.enro.core.hosts.AbstractFragmentHostForComposable
import dev.enro.destination.activity.containerManager
import dev.enro.destination.compose.containerManager
import dev.enro.destination.fragment.containerManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import java.util.*
import kotlin.reflect.KClass

fun launchComposableRoot(): TestNavigationContext<ComposableDestination, ComposableDestinations.Root> {
    ActivityScenario.launch(DefaultActivity::class.java)

    expectContext<DefaultActivity, DefaultActivityKey>()
        .navigation
        .replaceRoot(ComposableDestinations.Root())

    return expectContext<ComposableDestination, ComposableDestinations.Root>().also {
        waitFor { it.context.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) }
    }
}

inline fun <reified NK: NavigationKey.SupportsPresent> launchComposable(navigationKey: NK): TestNavigationContext<ComposableDestination, NK> {
    ActivityScenario.launch(DefaultActivity::class.java)

    expectContext<DefaultActivity, DefaultActivityKey>()
        .navigation
        .replaceRoot(navigationKey)

    return expectContext()
}

fun launchFragmentRoot(): TestNavigationContext<FragmentDestinationRoot, FragmentDestinations.Root> {
    ActivityScenario.launch(DefaultActivity::class.java)

    expectContext<DefaultActivity, DefaultActivityKey>()
        .navigation
        .replaceRoot(FragmentDestinations.Root())

    return expectContext<FragmentDestinationRoot, FragmentDestinations.Root>().also {
        waitFor { it.context.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) }
    }
}

inline fun <reified NK: NavigationKey.SupportsPresent> launchFragment(navigationKey: NK): TestNavigationContext<Fragment, NK> {
    ActivityScenario.launch(DefaultActivity::class.java)

    expectContext<DefaultActivity, DefaultActivityKey>()
        .navigation
        .replaceRoot(navigationKey)

    return expectContext<Fragment, NK>().also {
        waitFor { it.context.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) }
    }
}

sealed class ContainerType
object IntoSameContainer : ContainerType()
object IntoChildContainer : ContainerType()
object IntoSiblingContainer : ContainerType()

inline fun <reified Context : Any, reified Key : NavigationKey.SupportsPush> TestNavigationContext<out Any, out NavigationKey>.assertPushesTo(
    containerType: ContainerType,
    expected: Key = Key::class.createFromDefaultConstructor(),
): TestNavigationContext<Context, Key> {
    // TODO these waitFors aren't ideal, would like to remove if possible.
    waitFor { navigationContext.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) }
    navigation.push(expected)
    val expectedContext = expectContext<Context, Key> { it.navigation.key == expected }
    assertEquals(expected, expectedContext.navigation.key)
    waitFor { expectedContext.navigationContext.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) }
    assertPushContainerType(
        pushFrom = this,
        pushOpened = expectedContext,
        containerType = containerType
    )
    return expectedContext
}

@OptIn(AdvancedEnroApi::class)
fun assertPushContainerType(
    pushFrom: TestNavigationContext<out Any, out NavigationKey>,
    pushOpened: TestNavigationContext<out Any, out NavigationKey>,
    containerType: ContainerType,
) {
    InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
        fun NavigationContainer.hasActiveContext(navigationContext: NavigationContext<*>): Boolean {
            val isActiveContextComposeHost =
                childContext?.contextReference is AbstractFragmentHostForComposable

            val isActiveContextInChildContainer =
                childContext?.containerManager?.activeContainer?.childContext == navigationContext

            return childContext == navigationContext || (isActiveContextComposeHost && isActiveContextInChildContainer)
        }

        fun <T : Any> withParentContext(parentContext: NavigationContext<*>?, block: (parentContext: NavigationContext<*>) -> T?) : T? {
            parentContext ?: return null

            val result = block(parentContext)
            return when {
                result != null -> result
                parentContext.contextReference is NavigationHost -> withParentContext(parentContext.parentContext, block)
                else -> null
            }
        }

        fun getParentContainer(fromContext: NavigationContext<*>?, block: (navigationContainer: NavigationContainer) -> Boolean) : NavigationContainer? {
            if (fromContext == null) return null
            val parentContainer = fromContext.parentContainer()
            if (parentContainer?.let(block) == true) return parentContainer

            val parentContext = fromContext.parentContext
            return when (parentContext?.contextReference) {
                is NavigationHost -> getParentContainer(parentContext, block)
                else -> null
            }
        }

        val container = when (containerType) {
            is IntoSameContainer -> getParentContainer(pushFrom.navigationContext) { parentContainer ->
                parentContainer.hasActiveContext(pushOpened.navigationContext)
            }
            is IntoChildContainer -> pushFrom.navigationContext
                .containerManager
                .containers
                .firstOrNull {
                    it.hasActiveContext(pushOpened.navigationContext)
                }
            is IntoSiblingContainer -> withParentContext(pushFrom.navigationContext.parentContext) { parentContext ->
                parentContext
                    .containerManager
                    .containers
                    .firstOrNull {
                        it.hasActiveContext(pushOpened.navigationContext) &&
                                !it.backstack.contains(pushFrom.navigation.instruction)
                    }
            }
        }

        assertNotNull(container)
    }
}

fun <Context : Any, Key : NavigationKey> TestNavigationContext<Context, Key>.assertIsChildOf(
    container: TestNavigationContext<out Any, out NavigationKey>
): TestNavigationContext<Context, Key> {
    val containerManager = when (container.context) {
        is ComponentActivity -> container.context.containerManager
        is Fragment -> container.context.containerManager
        is ComposableDestination -> container.context.containerManager
        else -> throw IllegalStateException()
    }

    val containingContainer = containerManager.containers.firstOrNull {
        it.childContext?.contextReference == context
    }
    assertNotNull(containingContainer)
    return this
}

inline fun <reified Context : Any, reified Key : NavigationKey.SupportsPush.WithResult<TestResult>> TestNavigationContext<out Any, out NavigationKey>.assertPushesForResultTo(
    containerType: ContainerType,
    expected: Key = Key::class.createFromDefaultConstructor()
): TestNavigationContext<Context, Key> {
    when (context) {
        is ComposableDestination -> context.resultChannel.push(expected)
        is FragmentDestinations.Fragment -> context.resultChannel.push(expected)
        is ActivityDestinations.Activity -> context.resultChannel.push(expected)
        else -> throw IllegalStateException()
    }
    val expectedContext = expectContext<Context, Key> { it.navigation.key == expected }
    assertEquals(expected, expectedContext.navigation.key)
    assertPushContainerType(
        pushFrom = this,
        pushOpened = expectedContext,
        containerType = containerType
    )
    return expectedContext
}

inline fun <reified Context : Any, reified Key : NavigationKey.SupportsPresent> TestNavigationContext<out Any, out NavigationKey>.assertPresentsTo(
    expected: Key = Key::class.createFromDefaultConstructor()
): TestNavigationContext<Context, Key> {
    navigation.present(expected)
    val expectedContext = expectContext<Context, Key> { it.navigation.key == expected }
    assertEquals(expected, expectedContext.navigation.key)
    return expectedContext
}

inline fun <reified Context : Any, reified Key : NavigationKey.SupportsPresent.WithResult<TestResult>> TestNavigationContext<out Any, out NavigationKey>.assertPresentsForResultTo(
    expected: Key = Key::class.createFromDefaultConstructor()
): TestNavigationContext<Context, Key> {
    when (context) {
        is ComposableDestination -> context.resultChannel.present(expected)
        is FragmentDestinations.Fragment -> context.resultChannel.present(expected)
        is ActivityDestinations.Activity -> context.resultChannel.present(expected)
        else -> throw IllegalStateException()
    }
    val expectedContext = expectContext<Context, Key> { it.navigation.key == expected }
    assertEquals(expected, expectedContext.navigation.key)
    return expectedContext
}

inline fun <reified Context : Any, reified Key : NavigationKey.SupportsPresent> TestNavigationContext<out Any, out NavigationKey>.assertReplacesRootTo(
    expected: Key = Key::class.createFromDefaultConstructor()
): TestNavigationContext<Context, Key> {
    navigation.replaceRoot(expected)
    val expectedContext = expectContext<Context, Key> { it.navigation.key == expected }
    assertEquals(expected, expectedContext.navigation.key)
    return expectedContext
}

inline fun <reified Context : Any, reified Key : NavigationKey> TestNavigationContext<out Any, out NavigationKey>.assertClosesTo(
    expected: Key
): TestNavigationContext<Context, Key> {
    navigation.close()
    val expectedContext = expectContext<Context, Key> { it.navigation.key == expected }
    assertEquals(expected, expectedContext.navigation.key)
    return expectedContext
}

fun TestNavigationContext<out Any, out NavigationKey>.assertClosesToNothing() {
    navigation.close()
    expectNoActivity()
}

inline fun <reified Context : Any, reified Key : NavigationKey> TestNavigationContext<out Any, out NavigationKey.WithResult<TestResult>>.assertClosesWithResultTo(
    expected: Key
): TestNavigationContext<Context, Key> {
    val expectedResultId = UUID.randomUUID().toString()
    navigation.closeWithResult(TestResult(expectedResultId))

    val expectedContext = expectContext<Context, Key> {
        it.navigation.key == expected && it.navigation.hasTestResult()
    }
    assertEquals(expected, expectedContext.navigation.key)
    assertEquals(expectedResultId, expectedContext.navigation.expectTestResult().id)
    return expectedContext
}

fun <T : Any> KClass<T>.createFromDefaultConstructor(): T {
    return constructors
        .first { constructor ->
            constructor.parameters.all { parameter ->
                parameter.isOptional
            }
        }
        .callBy(emptyMap())
}