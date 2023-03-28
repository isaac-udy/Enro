package dev.enro.test

import android.os.Bundle
import androidx.core.os.bundleOf
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationContainerContext
import dev.enro.core.container.emptyBackstack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals

public class TestNavigationContainer(
    val key: NavigationContainerKey,
    backstack: NavigationBackstack,
) : NavigationContainerContext {
    private val mutableBackstackFlow = MutableStateFlow(backstack)
    override val backstackFlow: StateFlow<NavigationBackstack> = mutableBackstackFlow
    override val backstack: NavigationBackstack get() = backstackFlow.value

    override fun setBackstack(backstack: NavigationBackstack) {
        mutableBackstackFlow.value = backstack
    }

    override val isActive: Boolean = true
    override fun setActive() {}

    override fun save(): Bundle {
        return bundleOf(
            BACKSTACK_KEY to ArrayList(backstack)
        )
    }

    override fun restore(bundle: Bundle) {
        TODO("Not yet implemented")
    }

    companion object {
        private const val BACKSTACK_KEY = "TestNavigationContainer.BACKSTACK_KEY"
        val parentContainer = NavigationContainerKey.FromName("TestNavigationContainer.parentKey")
        val activeContainer = NavigationContainerKey.FromName("TestNavigationContainer.activeContainer")
    }
}

fun createTestNavigationContainer(
    key: NavigationContainerKey,
    backstack: NavigationBackstack = emptyBackstack(),
) = TestNavigationContainer(key, backstack)

fun NavigationContainerContext.assertBackstackEquals(
    backstack: NavigationBackstack
) {
    assertEquals(backstack, this.backstack)
}

fun NavigationContainerContext.assertContains(
    instruction: NavigationInstruction.Open<*>
) {
    assertEquals(
        instruction,
        backstack.firstOrNull { it == instruction }
    )
}

fun NavigationContainerContext.assertContains(
    key: NavigationKey
) {
    assertEquals(
        key,
        backstack.map { it.navigationKey }
            .firstOrNull { it == key }
    )
}

fun NavigationContainerContext.assertDoesNotContain(
    instruction: NavigationInstruction.Open<*>
) {
    assertEquals(
        null,
        backstack.firstOrNull { it == instruction }
    )
}

fun NavigationContainerContext.assertDoesNotContain(
    key: NavigationKey
) {
    assertEquals(
        null,
        backstack.map { it.navigationKey }
            .firstOrNull { it == key }
    )
}

fun NavigationContainerContext.assertActive(
    instruction: NavigationInstruction.Open<*>
) {
    assertEquals(instruction, backstack.active)
}

fun NavigationContainerContext.assertActive(
    key: NavigationKey
) {
    assertEquals(key, backstack.active?.navigationKey)
}

fun NavigationContainerContext.assertNotActive(
    instruction: NavigationInstruction.Open<*>
) {
    assertNotEquals(instruction, backstack.active)
}

fun NavigationContainerContext.assertNotActive(
    key: NavigationKey
) {
    assertNotEquals(key, backstack.active?.navigationKey)
}