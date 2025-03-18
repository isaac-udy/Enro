@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro.test

import androidx.lifecycle.Lifecycle
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.TypedNavigationHandle
import dev.enro.core.close
import dev.enro.core.controller.EnroDependencyScope
import java.lang.ref.WeakReference

class TestNavigationHandle<T : NavigationKey>(
    internal val navigationHandle: NavigationHandle
) : TypedNavigationHandle<T> {
    override val id: String
        get() = navigationHandle.id

    override val key: T
        get() = navigationHandle.key as T

    override val instruction: NavigationInstruction.Open<*>
        get() = navigationHandle.instruction

    override val dependencyScope: EnroDependencyScope
        get() = navigationHandle.dependencyScope

    internal var internalOnCloseRequested: () -> Unit = { close() }

    override val lifecycle: Lifecycle
        get() {
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
        if (instructions.lastOrNull() is NavigationInstruction.Close) {
            throw IllegalStateException("TestNavigationHandle has received a close instruction and can no longer execute instructions")
        }
        allInstructions.add(navigationInstruction)
        navigationHandle.executeInstruction(navigationInstruction)
    }

    companion object {
        internal val allInstructions = mutableListOf<NavigationInstruction>()
    }
}

/**
 * Create a TestNavigationHandle to be used in tests.
 */
fun <T : NavigationKey> createTestNavigationHandle(
    key: T,
): TestNavigationHandle<T> {
    lateinit var navigationHandle: WeakReference<TestNavigationHandle<T>>
    val fakeNavigationHandle = FakeNavigationHandle(key) {
        navigationHandle.get()?.internalOnCloseRequested?.invoke()
    }
    navigationHandle = WeakReference(TestNavigationHandle(fakeNavigationHandle))
    return navigationHandle.get()!!
}

