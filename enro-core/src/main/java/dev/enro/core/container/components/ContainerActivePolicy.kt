package dev.enro.core.container.components

import androidx.lifecycle.Lifecycle
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.container.NavigationBackstackTransition

public interface ContainerActivePolicy {

    public val isActive: Boolean
    public fun setActive()

    public fun setActiveContainerFrom(backstackTransition: NavigationBackstackTransition)

    public class Default(
        private val key: NavigationContainerKey,
        private val context: NavigationContext<*>,
    ) : ContainerActivePolicy {

        override val isActive: Boolean
            get() = context.containerManager.activeContainer?.key == key

        public override fun setActive() {
            context.containerManager.setActiveContainerByKey(key)
        }

        public override fun setActiveContainerFrom(
            backstackTransition: NavigationBackstackTransition
        ) {
            if (!context.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return
            if (context.containerManager.getContainer(key) == null) return

            val isClosing = backstackTransition.lastInstruction is NavigationInstruction.Close
            val isEmpty = backstackTransition.activeBackstack.isEmpty()

            if (!isClosing) {
                context.containerManager.setActiveContainerByKey(key)
                return
            }

            if (backstackTransition.exitingInstruction != null) {
                context.containerManager.setActiveContainerByKey(
                    backstackTransition.exitingInstruction.internal.previouslyActiveContainer
                )
            }

            if (isActive && isEmpty) context.containerManager.setActiveContainer(null)
        }
    }
}