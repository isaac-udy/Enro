package dev.enro.test

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.TypedNavigationHandle
import dev.enro.core.controller.NavigationController
import dev.enro.test.extensions.getTestResultForId
import junit.framework.TestCase
import org.junit.Assert.assertEquals

class TestNavigationHandle<T : NavigationKey>(
    private val navigationHandle: NavigationHandle
) : TypedNavigationHandle<T> {
    override val id: String
        get() = navigationHandle.id

    override val controller: NavigationController
        get() = navigationHandle.controller

    override val additionalData: Bundle
        get() = navigationHandle.additionalData

    override val key: T
        get() = navigationHandle.key as T

    override val instruction: NavigationInstruction.Open
        get() = navigationHandle.instruction

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
    key: NavigationKey
): TestNavigationHandle<T> {
    val instruction = NavigationInstruction.Forward(
        navigationKey = key
    )

    return TestNavigationHandle(object : NavigationHandle {
        private val instructions = mutableListOf<NavigationInstruction>()

        @SuppressLint("VisibleForTests")
        private val lifecycle = LifecycleRegistry.createUnsafe(this).apply {
            currentState = Lifecycle.State.RESUMED
        }

        override val id: String = instruction.instructionId
        override val additionalData: Bundle = instruction.additionalData
        override val key: NavigationKey = key
        override val instruction: NavigationInstruction.Open = instruction

        override val controller: NavigationController = EnroTest.getCurrentNavigationController()

        override fun executeInstruction(navigationInstruction: NavigationInstruction) {
            instructions.add(navigationInstruction)
        }

        override fun getLifecycle(): Lifecycle {
            return lifecycle
        }
    })
}

fun TestNavigationHandle<*>.expectCloseInstruction() {
    TestCase.assertTrue(instructions.last() is NavigationInstruction.Close)
}

fun <T : Any> TestNavigationHandle<*>.expectOpenInstruction(type: Class<T>): NavigationInstruction.Open {
    val instruction = instructions.last()
    TestCase.assertTrue(instruction is NavigationInstruction.Open)
    instruction as NavigationInstruction.Open

    TestCase.assertTrue(type.isAssignableFrom(instruction.navigationKey::class.java))
    return instruction
}

inline fun <reified T : Any> TestNavigationHandle<*>.expectOpenInstruction(): NavigationInstruction.Open {
    return expectOpenInstruction(T::class.java)
}

fun <T: Any> TestNavigationHandle<*>.expectResult(expected: T) {
    val result = getTestResultForId(id)
    assertEquals(expected, result)
}