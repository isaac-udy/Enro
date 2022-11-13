@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
package dev.enro.test

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import dev.enro.core.*
import dev.enro.core.internal.handle.NavigationHandleScope
import junit.framework.TestCase
import org.junit.Assert.*
import java.lang.ref.WeakReference

class TestNavigationHandle<T : NavigationKey>(
    private val navigationHandle: NavigationHandle
) : TypedNavigationHandle<T> {
    override val id: String
        get() = navigationHandle.id

    override val additionalData: Bundle
        get() = navigationHandle.additionalData

    override val key: T
        get() = navigationHandle.key as T

    override val instruction: NavigationInstruction.Open<*>
        get() = navigationHandle.instruction

    override val dependencyScope: EnroDependencyScope
        get() = navigationHandle.dependencyScope

    internal var internalOnCloseRequested: () -> Unit = { close() }

    override fun getLifecycle(): Lifecycle {
        return navigationHandle.lifecycle
    }

    val instructions: List<NavigationInstruction>
        get() = navigationHandle::class.java.getDeclaredField("instructions").let {
            it.isAccessible = true
            val instructions = it.get(navigationHandle)
            it.isAccessible = false
            return instructions as List<NavigationInstruction>
        }

    override fun executeInstruction(navigationInstruction: NavigationInstruction) {
        navigationHandle.executeInstruction(navigationInstruction)
    }
}

fun <T : NavigationKey> createTestNavigationHandle(
    key: T
): TestNavigationHandle<T> {
    val instruction = NavigationInstruction.Forward(
        navigationKey = key
    )
    lateinit var navigationHandle: WeakReference<TestNavigationHandle<T>>
    navigationHandle = WeakReference(TestNavigationHandle(object : NavigationHandle {
        private val instructions = mutableListOf<NavigationInstruction>()

        @SuppressLint("VisibleForTests")
        private val lifecycle = LifecycleRegistry.createUnsafe(this).apply {
            currentState = Lifecycle.State.RESUMED
        }

        override val id: String = instruction.instructionId
        override val additionalData: Bundle = instruction.additionalData
        override val key: NavigationKey = key
        override val instruction: NavigationInstruction.Open<*> = instruction
        override val dependencyScope: EnroDependencyScope = NavigationHandleScope(
            EnroTest.getCurrentNavigationController()
        ).bind(this)

        override fun executeInstruction(navigationInstruction: NavigationInstruction) {
            instructions.add(navigationInstruction)
            if(navigationInstruction is NavigationInstruction.RequestClose) {
                navigationHandle.get()?.internalOnCloseRequested?.invoke()
            }
        }

        override fun getLifecycle(): Lifecycle {
            return lifecycle
        }
    }))
    return navigationHandle.get()!!
}

fun TestNavigationHandle<*>.expectCloseInstruction() {
    TestCase.assertTrue(instructions.last() is NavigationInstruction.Close)
}

fun <T : Any> TestNavigationHandle<*>.expectOpenInstruction(type: Class<T>): NavigationInstruction.Open<*> {
    val instruction = instructions.filterIsInstance<NavigationInstruction.Open<*>>().last()
    assertTrue(type.isAssignableFrom(instruction.navigationKey::class.java))
    return instruction
}

inline fun <reified T : Any> TestNavigationHandle<*>.expectOpenInstruction(): NavigationInstruction.Open<*> {
    return expectOpenInstruction(T::class.java)
}

fun TestNavigationHandle<*>.assertRequestedClose() {
    val instruction = instructions.filterIsInstance<NavigationInstruction.RequestClose>()
        .lastOrNull()
    assertNotNull(instruction)
}

fun TestNavigationHandle<*>.assertClosed() {
    val instruction = instructions.filterIsInstance<NavigationInstruction.Close>()
        .lastOrNull()
    assertNotNull(instruction)
}

fun TestNavigationHandle<*>.assertNotClosed() {
    val instruction = instructions.filterIsInstance<NavigationInstruction.Close>()
        .lastOrNull()
    assertNull(instruction)
}

fun <T : Any> TestNavigationHandle<*>.assertOpened(type: Class<T>, direction: NavigationDirection? = null): T {
    val instruction = instructions.filterIsInstance<NavigationInstruction.Open<*>>()
        .lastOrNull()

    assertNotNull(instruction)
    requireNotNull(instruction)

    assertTrue(type.isAssignableFrom(instruction.navigationKey::class.java))
    if(direction != null) {
        assertEquals(direction, instruction.navigationDirection)
    }
    return instruction.navigationKey as T
}

inline fun <reified T : Any> TestNavigationHandle<*>.assertOpened(direction: NavigationDirection? = null): T {
    return assertOpened(T::class.java, direction)
}

fun <T : Any> TestNavigationHandle<*>.assertAnyOpened(type: Class<T>, direction: NavigationDirection? = null): T {
    val instruction = instructions.filterIsInstance<NavigationInstruction.Open<*>>()
        .lastOrNull { type.isAssignableFrom(it.navigationKey::class.java) }

    assertNotNull(instruction)
    requireNotNull(instruction)

    assertTrue(type.isAssignableFrom(instruction.navigationKey::class.java))
    if(direction != null) {
        assertEquals(direction, instruction.navigationDirection)
    }
    return instruction.navigationKey as T
}

inline fun <reified T : Any> TestNavigationHandle<*>.assertAnyOpened(direction: NavigationDirection? = null): T {
    return assertAnyOpened(T::class.java, direction)
}

fun TestNavigationHandle<*>.assertNoneOpened() {
    val instruction = instructions.filterIsInstance<NavigationInstruction.Open<*>>()
        .lastOrNull()
    assertNull(instruction)
}

internal fun TestNavigationHandle<*>.getResult(): Any? {
    return instructions.filterIsInstance<NavigationInstruction.Close.WithResult>()
        .lastOrNull()
        ?.result
}

fun <T: Any> TestNavigationHandle<*>.assertResultDelivered(predicate: (T) -> Boolean): T {
    val result = getResult()
    assertNotNull(result)
    requireNotNull(result)
    result as T
    assertTrue(predicate(result))
    return result
}

fun <T: Any> TestNavigationHandle<*>.assertResultDelivered(expected: T): T {
    val result = getResult()
    assertEquals(expected, result)
    return result as T
}

inline fun <reified T: Any> TestNavigationHandle<*>.assertResultDelivered(): T {
    return assertResultDelivered { true }
}

fun TestNavigationHandle<*>.assertNoResultDelivered() {
    val result = getResult()
    assertNull(result)
}