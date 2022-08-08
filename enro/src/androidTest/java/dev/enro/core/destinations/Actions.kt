package dev.enro.core.destinations

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import dev.enro.*
import dev.enro.core.*
import dev.enro.core.hosts.AbstractFragmentHostForComposable
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.container.NavigationContainer
import dev.enro.core.result.closeWithResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import java.util.*
import kotlin.reflect.KClass

fun launchComposableRoot(): TestNavigationContext<ComposableDestination, ComposableDestinations.Root> {
    ActivityScenario.launch(DefaultActivity::class.java)

    expectContext<DefaultActivity, DefaultActivityKey>()
        .navigation
        .replaceRoot(ComposableDestinations.Root())

    return expectContext()
}

fun launchFragmentRoot(): TestNavigationContext<FragmentDestinationRoot, FragmentDestinations.Root> {
    ActivityScenario.launch(DefaultActivity::class.java)

    expectContext<DefaultActivity, DefaultActivityKey>()
        .navigation
        .replaceRoot(FragmentDestinations.Root())

    return expectContext()
}

sealed class ContainerType
object IntoSameContainer : ContainerType()
object IntoChildContainer : ContainerType()
object IntoSiblingContainer : ContainerType()

inline fun <reified Context : Any, reified Key : NavigationKey.SupportsPush> TestNavigationContext<out Any, out NavigationKey>.assertPushesTo(
    containerType: ContainerType,
    expected: Key = Key::class.createFromDefaultConstructor(),
): TestNavigationContext<Context, Key> {
    navigation.push(expected)
    val expectedContext = expectContext<Context, Key> { it.navigation.key == expected }
    assertEquals(expected, expectedContext.navigation.key)
    assertPushContainerType(
        pushFrom = this,
        pushOpened = expectedContext,
        containerType = containerType
    )
    return expectedContext
}

fun assertPushContainerType(
    pushFrom: TestNavigationContext<out Any, out NavigationKey>,
    pushOpened: TestNavigationContext<out Any, out NavigationKey>,
    containerType: ContainerType,
) {
    InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
        val parentContext = run {
            val it = pushFrom.navigationContext.parentContext()!!
            if (it.contextReference is AbstractFragmentHostForComposable) it.parentContext()!! else it
        }

        fun NavigationContainer.hasActiveContext(navigationContext: NavigationContext<*>): Boolean {
            val isActiveContextComposeHost =
                activeContext?.contextReference is AbstractFragmentHostForComposable

            val isActiveContextInChildContainer =
                activeContext?.containerManager?.activeContainer?.activeContext == navigationContext

            return activeContext == navigationContext || (isActiveContextComposeHost && isActiveContextInChildContainer)
        }

        val container = when (containerType) {
            is IntoSameContainer -> parentContext
                .containerManager
                .containers
                .firstOrNull {
                    it.backstackFlow.value.backstack.contains(pushFrom.navigation.instruction)
                }
            is IntoChildContainer -> pushFrom.navigationContext
                .containerManager
                .containers
                .firstOrNull {
                    it.hasActiveContext(pushOpened.navigationContext)
                }
            is IntoSiblingContainer -> parentContext
                .containerManager
                .containers
                .firstOrNull {
                    it.hasActiveContext(pushOpened.navigationContext) &&
                            !it.backstackFlow.value.backstack.contains(pushFrom.navigation.instruction)
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
        it.activeContext?.contextReference == context
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