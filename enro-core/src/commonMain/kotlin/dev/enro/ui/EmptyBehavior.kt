package dev.enro.ui

import dev.enro.NavigationBackstack
import dev.enro.NavigationTransition

public class EmptyBehavior internal constructor(
    private val isAnimationEnabled: AnimationEnabledScope.() -> Boolean,
    private val onEmpty: OnEmptyScope.() -> Unit,
) {
    public fun isAnimationEnabled(
        transition: NavigationTransition,
    ): Boolean {
        if (transition.targetBackstack.isNotEmpty()) return true
        return isAnimationEnabled(AnimationEnabledScope(transition.currentBackstack))
    }

    public class AnimationEnabledScope internal constructor(
        public val currentBackstack: NavigationBackstack,
    )

    public class OnEmptyScope internal constructor() {

    }

    public companion object {
        public fun allowEmpty(): EmptyBehavior {
            return EmptyBehavior(
                isAnimationEnabled = { true },
                onEmpty = {},
            )
        }

        public fun closeParent(): EmptyBehavior {
            return EmptyBehavior(
                isAnimationEnabled = { false },
                onEmpty = {},
            )
        }

        public fun default(): EmptyBehavior {
            return closeParent()
        }
    }

}