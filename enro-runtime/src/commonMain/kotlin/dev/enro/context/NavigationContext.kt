package dev.enro.context

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.EnroController

public sealed interface NavigationContextBase
public typealias AnyNavigationContext = NavigationContext<*, *>

public sealed class NavigationContext<Parent, Child : NavigationContextBase>() :
    NavigationContextBase,
    LifecycleOwner,
    ViewModelStoreOwner,
    HasDefaultViewModelProviderFactory {

    public abstract val id: String
    public abstract val controller: EnroController

    // True if this context is the active child of the parent context, or has no parent context.
    public val isActive: Boolean by derivedStateOf {
        val parentContext = parent as? NavigationContext<*, *>
        if (parentContext == null) return@derivedStateOf true
        return@derivedStateOf parentContext.activeChild == this
    }

    // True if this context and each ancestor context is active.
    public val isActiveInRoot: Boolean by derivedStateOf {
        val parentContext = parent as? NavigationContext<*, *>
        isActive && (parentContext == null || parentContext.isActiveInRoot)
    }
    public abstract val parent: Parent

    protected val mutableChildren: SnapshotStateMap<String, ChildState<Child>> = mutableStateMapOf()
    public val children: List<Child> by derivedStateOf {
        mutableChildren.values.map { it.child }
    }

    public abstract val activeChild: Child?

    public abstract fun registerChild(child: Child)
    public abstract fun unregisterChild(child: Child)

    public abstract fun registerVisibility(child: Child, isVisible: Boolean)

    // Makes a ContainerContext the active container of the parent context. The parent context
    // is not made active. A DestinationContext forwards the request to the parent container.
    public fun requestActive() {
        when (this) {
            is ContainerContext -> {
                parent.setActiveContainer(this)
            }

            is DestinationContext<*> -> {
                parent.requestActive()
            }

            is RootContext -> {
                // A RootContext cannot be made active.
            }
        }
    }

    // Calls requestActive on this context and on each ancestor context up to the root.
    public fun requestActiveInRoot() {
        requestActive()
        when (this) {
            is ContainerContext -> {
                parent.requestActiveInRoot()
            }

            is DestinationContext<*> -> {
                parent.requestActiveInRoot()
            }

            is RootContext -> {
                // A RootContext cannot be made active.
            }
        }
    }

    protected data class ChildState<NavigationContextBase>(
        val child: NavigationContextBase,
        val isVisible: Boolean,
        val registrationOrder: Long,
    )

    internal companion object {
        internal var registrationOrderCounter = 0L
    }

    public sealed class WithContainerChildren<Parent>(
        private val activeChildId: MutableState<String?>,
    ) : NavigationContext<Parent, ContainerContext>() {
        override val activeChild: ContainerContext? by derivedStateOf {
            mutableChildren[activeChildId.value]?.child
        }

        override fun registerChild(child: ContainerContext) {
            mutableChildren[child.id] = ChildState(
                child = child,
                isVisible = false,
                registrationOrder = registrationOrderCounter++,
            )
            if (activeChildId.value == null) {
                activeChildId.value = child.id
            }
        }

        override fun unregisterChild(child: ContainerContext) {
            mutableChildren.remove(child.id)
            if (activeChildId.value == child.id) {
                activeChildId.value = mutableChildren.values
                    .sortedBy { it.registrationOrder }
                    .firstOrNull {
                        it.isVisible
                    }?.child?.id
            }
        }

        override fun registerVisibility(
            child: ContainerContext,
            isVisible: Boolean,
        ) {
            val current = mutableChildren[child.id]
            if (current == null) return
            if (current.isVisible == isVisible) return
            mutableChildren[child.id] = ChildState(
                child = child,
                isVisible = isVisible,
                registrationOrder = current.registrationOrder
            )

            val currentlyActive = mutableChildren[activeChildId.value]
            if (currentlyActive == null || !currentlyActive.isVisible) {
                if (isVisible) {
                    activeChildId.value = child.id
                }
            }
            if (!isVisible && activeChildId.value == child.id) {
                val visibleChild = mutableChildren.values
                    .sortedBy { it.registrationOrder }
                    .firstOrNull {
                        it.isVisible
                    }?.child
                if (visibleChild != null) {
                    activeChildId.value = visibleChild.id
                }
            }
        }

        public fun setActiveContainer(childId: String) {
            val child = children.firstOrNull { it.id == childId }
            if (child != null) {
                activeChildId.value = child.id
            }
        }
    }
}
