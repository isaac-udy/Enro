package dev.enro.core.controller.usecase

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.animation.NavigationAnimation
import dev.enro.animation.NavigationAnimationOverrideBuilder
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationKey
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationBackstackTransition
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.acceptAll
import dev.enro.core.container.close
import dev.enro.core.container.emptyBackstack
import dev.enro.core.container.present
import dev.enro.core.container.push
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.internalCreateNavigationController
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class GetAnimationsForTransitionTest {

    val controller = internalCreateNavigationController {
        animations {
            defaults(NavigationAnimationForTest.Defaults)
        }
    }

    @Test
    fun push_transitionTo_pushPush() {
        val initialBackstack = emptyBackstack()
            .push(PushKey(0))

        val updatedBackstack = initialBackstack
            .push(PushKey(1))

        val animations = testAnimationsFor(initialBackstack to updatedBackstack)
        assertEquals(
            NavigationAnimationForTest.Defaults.push,
            animations[initialBackstack[0].instructionId]
        )
        assertEquals(
            NavigationAnimationForTest.Defaults.push,
            animations[updatedBackstack[1].instructionId]
        )
    }

    @Test
    fun pushPush_transitionTo_push() {
        val initialBackstack = emptyBackstack()
            .push(PushKey(0))
            .push(PushKey(1))

        val updatedBackstack = initialBackstack
            .close(initialBackstack[1].instructionId)

        val animations = testAnimationsFor(initialBackstack to updatedBackstack)

        // When an instruction is pushed, the currently active pushed instruction should
        // not need to perform any animation
        assertEquals(
            NavigationAnimationForTest.Defaults.pushReturn,
            animations[initialBackstack[0].instructionId]
        )
        assertEquals(
            NavigationAnimationForTest.Defaults.pushReturn,
            animations[initialBackstack[1].instructionId]
        )
    }

    @Test
    fun empty_transitionTo_push() {
        val initialBackstack = emptyBackstack()

        val updatedBackstack = initialBackstack
            .push(PushKey(0))

        val animations = testAnimationsFor(initialBackstack to updatedBackstack)

        assertEquals(
            NavigationAnimationForTest.Defaults.push,
            animations[updatedBackstack[0].instructionId]
        )
    }

    @Test
    fun push_transitionTo_empty() {
        val initialBackstack = emptyBackstack()
            .push(PushKey(0))

        val updatedBackstack = emptyBackstack()

        val animations = testAnimationsFor(initialBackstack to updatedBackstack)

        assertEquals(
            NavigationAnimationForTest.Defaults.pushReturn,
            animations[initialBackstack[0].instructionId]
        )
    }


    @Test
    fun push_transitionTo_pushPresent() {
        val initialBackstack = emptyBackstack()
            .push(PushKey(0))

        val updatedBackstack = initialBackstack
            .present(PresentKey(1))

        val animations = testAnimationsFor(initialBackstack to updatedBackstack)

        // When an instruction is presented, the currently active pushed instruction should
        // not need to perform any animation
        assertNull(
            animations[initialBackstack[0].instructionId]
        )
        assertEquals(
            NavigationAnimationForTest.Defaults.present,
            animations[updatedBackstack[1].instructionId]
        )
    }

    @Test
    fun pushPresent_transitionTo_push() {
        val initialBackstack = emptyBackstack()
            .push(PushKey(0))
            .present(PresentKey(1))

        val updatedBackstack = initialBackstack
            .close(initialBackstack[1].instructionId)

        val animations = testAnimationsFor(initialBackstack to updatedBackstack)

        // When a presented animation is closed, the currently active pushed instruction should
        // not need to perform any animation
        assertNull(
            animations[initialBackstack[0].instructionId]
        )
        assertEquals(
            NavigationAnimationForTest.Defaults.presentReturn,
            animations[initialBackstack[1].instructionId]
        )
    }

    @Test
    fun pushPresent_transitionTo_pushPresentPresent() {
        val initialBackstack = emptyBackstack()
            .push(PushKey(0))
            .present(PresentKey(1))

        val updatedBackstack = initialBackstack
            .present(PresentKey(2))

        val animations = testAnimationsFor(initialBackstack to updatedBackstack)

        assertNull(
            animations[updatedBackstack[0].instructionId]
        )
        assertEquals(
            NavigationAnimationForTest.Defaults.present,
            animations[updatedBackstack[1].instructionId]
        )
        assertEquals(
            NavigationAnimationForTest.Defaults.present,
            animations[updatedBackstack[2].instructionId]
        )
    }

    @Test
    fun pushPresentPresent_transitionTo_pushPresent() {
        val initialBackstack = emptyBackstack()
            .push(PushKey(0))
            .present(PresentKey(1))
            .present(PresentKey(1))

        val updatedBackstack = initialBackstack
            .close(initialBackstack[2])

        val animations = testAnimationsFor(initialBackstack to updatedBackstack)

        assertNull(
            animations[initialBackstack[0].instructionId]
        )
        assertEquals(
            NavigationAnimationForTest.Defaults.presentReturn,
            animations[initialBackstack[1].instructionId]
        )
        assertEquals(
            NavigationAnimationForTest.Defaults.presentReturn,
            animations[initialBackstack[2].instructionId]
        )
    }

    @Test
    fun fourPushed_transitionTo_fivePushed() {
        val initialBackstack = emptyBackstack()
            .push(PushKey(0))
            .push(PushKey(1))
            .push(PushKey(2))
            .push(PushKey(3))

        val updatedBackstack = initialBackstack
            .push(PushKey(4))

        val animations = testAnimationsFor(initialBackstack to updatedBackstack)

        assertNull(animations[updatedBackstack[0].instructionId])
        assertNull(animations[updatedBackstack[1].instructionId])
        assertNull(animations[updatedBackstack[2].instructionId])
        assertEquals(
            NavigationAnimationForTest.Defaults.push,
            animations[updatedBackstack[3].instructionId]
        )
        assertEquals(
            NavigationAnimationForTest.Defaults.push,
            animations[updatedBackstack[4].instructionId]
        )
    }

    @Test
    fun fivePushed_transitionTo_fourPushed() {
        val initialBackstack = emptyBackstack()
            .push(PushKey(0))
            .push(PushKey(1))
            .push(PushKey(2))
            .push(PushKey(3))
            .push(PushKey(4))

        val updatedBackstack = initialBackstack
            .close(initialBackstack[4])

        val animations = testAnimationsFor(initialBackstack to updatedBackstack)

        assertNull(animations[initialBackstack[0].instructionId])
        assertNull(animations[initialBackstack[1].instructionId])
        assertNull(animations[initialBackstack[2].instructionId])
        assertEquals(
            NavigationAnimationForTest.Defaults.pushReturn,
            animations[initialBackstack[3].instructionId]
        )
        assertEquals(
            NavigationAnimationForTest.Defaults.pushReturn,
            animations[initialBackstack[4].instructionId]
        )
    }

    @Test
    fun closingMiddlePushedElementsCausesNoAnimation() {
        val initialBackstack = emptyBackstack()
            .push(PushKey(0))
            .push(PushKey(1))
            .push(PushKey(2))
            .push(PushKey(3))
            .push(PushKey(4))

        val updatedBackstack = initialBackstack
            .close(initialBackstack[1])
            .close(initialBackstack[2])
            .close(initialBackstack[3])

        val animations = testAnimationsFor(initialBackstack to updatedBackstack)

        assertNull(animations[initialBackstack[0].instructionId])
        assertNull(animations[initialBackstack[1].instructionId])
        assertNull(animations[initialBackstack[2].instructionId])
        assertNull(animations[initialBackstack[3].instructionId])
        assertNull(animations[initialBackstack[4].instructionId])
    }
    @Test
    fun pushPresent_transitionTo_pushPresentPush() {
        val initialBackstack = emptyBackstack()
            .push(PushKey(0))
            .present(PresentKey(1))

        val updatedBackstack = initialBackstack
            .push(PushKey(2))

        val animations = testAnimationsFor(initialBackstack to updatedBackstack)

        assertEquals(
            NavigationAnimationForTest.Defaults.push,
            animations[updatedBackstack[0].instructionId]
        )
        assertEquals(
            NavigationAnimationForTest.Defaults.present,
            animations[updatedBackstack[1].instructionId]
        )
        assertEquals(
            NavigationAnimationForTest.Defaults.push,
            animations[updatedBackstack[2].instructionId]
        )
    }

    @Test
    fun pushPresentPush_transitionTo_pushPresent() {
        val initialBackstack = emptyBackstack()
            .push(PushKey(0))
            .present(PresentKey(1))
            .push(PushKey(2))

        val updatedBackstack = initialBackstack
            .close(initialBackstack[2])

        val animations = testAnimationsFor(initialBackstack to updatedBackstack)

        assertEquals(
            NavigationAnimationForTest.Defaults.pushReturn,
            animations[initialBackstack[0].instructionId]
        )
        assertEquals(
            NavigationAnimationForTest.Defaults.presentReturn,
            animations[initialBackstack[1].instructionId]
        )
        assertEquals(
            NavigationAnimationForTest.Defaults.pushReturn,
            animations[initialBackstack[2].instructionId]
        )
    }

    data class PresentKey(val id: Int) : NavigationKey.SupportsPresent
    data class PushKey(val id: Int) : NavigationKey.SupportsPush

    fun testAnimationsFor(
        transition: Pair<NavigationBackstack, NavigationBackstack>
    ): Map<String, NavigationAnimationForTest> {
        return GetAnimationsForTransition().getAnimations(
            type = NavigationAnimationForTest::class,
            container = AnimationTestContainer(controller),
            transition = NavigationBackstackTransition(transition)
        )
    }

    class AnimationTestContainer(
        controller: NavigationController,
        animations: NavigationAnimationOverrideBuilder.() -> Unit = {},
    ) : NavigationContainer(
        key = NavigationContainerKey.FromName("test"),
        contextType = TestNavigationContext::class,
        context = NavigationContext(
            contextReference = TestNavigationContext,
            getController = { controller },
            getParentContext = { null },
            getContextInstruction = { null },
            getViewModelStoreOwner = { TestNavigationContext },
            getSavedStateRegistryOwner = { TestNavigationContext },
            getLifecycleOwner = { TestNavigationContext },
            onBoundToNavigationHandle = {},
        ),
        emptyBehavior = EmptyBehavior.AllowEmpty,
        interceptor = {},
        animations = animations,
        instructionFilter = acceptAll(),
    ) {
        override val isVisible: Boolean = true

        override fun getChildContext(contextFilter: ContextFilter): NavigationContext<*>? {
            return null
        }

        override fun onBackstackUpdated(
            transition: NavigationBackstackTransition,
            isLifecycleUpdate: Boolean
        ): Boolean {
            return true
        }
    }

    data class NavigationAnimationForTest(
        val name: String,
    ) : NavigationAnimation {
        companion object {
            val Defaults = NavigationAnimation.Defaults(
                none = NavigationAnimationForTest("none"),
                push = NavigationAnimationForTest("push"),
                pushReturn = NavigationAnimationForTest("pushReturn"),
                present = NavigationAnimationForTest("present"),
                presentReturn = NavigationAnimationForTest("presentReturn"),
            )
        }
    }

    object TestNavigationContext : SavedStateRegistryOwner, ViewModelStoreOwner {
        private val savedStateRegistryController = SavedStateRegistryController.create(this)
        override val savedStateRegistry: SavedStateRegistry =
            savedStateRegistryController.savedStateRegistry
        override val viewModelStore: ViewModelStore = ViewModelStore()
        private val lifecycleRegistry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = lifecycleRegistry
    }
}