package dev.enro.test

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ApplicationProvider
import dev.enro.core.*
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.navigationController
import junit.framework.TestCase

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

fun <T: NavigationKey> createTestNavigationHandle(
    key: NavigationKey
) : TestNavigationHandle<T> {
    val instruction = NavigationInstruction.Forward(
        navigationKey = key
    )

    return TestNavigationHandle(object: NavigationHandle {
        private val instructions = mutableListOf<NavigationInstruction>()
        private val lifecycle = LifecycleRegistry(this).apply {
            currentState = Lifecycle.State.RESUMED
        }

        override val id: String = instruction.instructionId
        override val additionalData: Bundle = instruction.additionalData
        override val key: NavigationKey = key

        override val controller: NavigationController = ApplicationProvider
            .getApplicationContext<Application>()
            .navigationController

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

fun <T: Any> TestNavigationHandle<*>.expectOpenInstruction(type: Class<T>): NavigationInstruction.Open<*> {
    val instruction = instructions.last()
    TestCase.assertTrue(instruction is NavigationInstruction.Open<*>)
    instruction as NavigationInstruction.Open<*>

    TestCase.assertTrue(type.isAssignableFrom(instruction.navigationKey::class.java))
    return instruction
}

inline fun <reified T: Any> TestNavigationHandle<*>.expectOpenInstruction(): NavigationInstruction.Open<*> {
    return expectOpenInstruction(T::class.java)
}