@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro.test

import androidx.lifecycle.Lifecycle
import dev.enro.core.*
import dev.enro.core.controller.EnroDependencyScope
import dev.enro.core.internal.handle.TestNavigationHandleViewModel
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
        get() = when(navigationHandle) {
            is TestNavigationHandleViewModel -> navigationHandle.instructions
            is FakeNavigationHandle -> navigationHandle.instructions
            else -> error("")
        }

    override fun executeInstruction(navigationInstruction: NavigationInstruction) {
        navigationHandle.executeInstruction(navigationInstruction)
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

