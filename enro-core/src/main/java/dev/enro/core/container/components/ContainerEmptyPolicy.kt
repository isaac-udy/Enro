package dev.enro.core.container.components

import androidx.lifecycle.Lifecycle
import dev.enro.core.NavigationContext
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.getNavigationHandle
import dev.enro.core.requestClose

public interface ContainerEmptyPolicy {
    public fun handleEmptyBehaviour(backstack: NavigationBackstack): Boolean

    public class Default(
        private val context: NavigationContext<*>,
        private val emptyBehavior: EmptyBehavior,
    ) : ContainerEmptyPolicy {
        public override fun handleEmptyBehaviour(backstack: NavigationBackstack): Boolean {
            if (!backstack.isEmpty()) return false

            when (val emptyBehavior = emptyBehavior) {
                EmptyBehavior.AllowEmpty -> {
                    /* If allow empty, pass through to default behavior */
                }

                EmptyBehavior.CloseParent -> {
                    if (context.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                        context.getNavigationHandle().requestClose()
                    }
                    return true
                }

                is EmptyBehavior.Action -> {
                    return emptyBehavior.onEmpty()
                }
            }
            return false
        }
    }
}