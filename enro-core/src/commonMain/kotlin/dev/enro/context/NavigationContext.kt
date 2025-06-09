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

    // Returns true if this NavigationContext can be considered active within the scope of it's parent
    public val isActive: Boolean by derivedStateOf {
        val parentContext = parent as? NavigationContext<*, *>
        if (parentContext == null) return@derivedStateOf true
        return@derivedStateOf parentContext.activeChild == this
    }

    // Returns true if this NavigationContext can be considered to be active globally,
    // in other words, is this context and its parent context considered active
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

    // requests that the current container becomes active
    // For NavigationContainer contexts, this will cause the NavigationContainer to become
    // active in its parent context (but not active globally)
    public fun requestActive() {
        when (this) {
            is ContainerContext -> {
                parent.setActiveContainer(this)
            }
            is DestinationContext<*> -> {
                // if a destination is requested to become active, we request that the parent container
                // becomes active
                parent.requestActive()
            }
            is RootContext -> {
                // RootContext does not have ability to request active
            }
        }
    }

    // requests that the current container becomes active globally, which is to say
    // that this container is requested to become active, and then
    // all parent containers are requested to become active recursively up until the root
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
                // RootContext does not have ability to request active
            }
        }
    }

    public data class ChildState<NavigationContextBase>(
        val child: NavigationContextBase,
        val isVisible: Boolean,
    )

    public sealed class WithContainerChildren<Parent>(
        private val activeChildId: MutableState<String?>
    ) : NavigationContext<Parent, ContainerContext>() {
        override val activeChild: ContainerContext? by derivedStateOf {
            mutableChildren[activeChildId.value]?.child
        }

        override fun registerChild(child: ContainerContext) {
            mutableChildren[child.id] = ChildState(child, false)
            if (activeChildId.value == null) {
                activeChildId.value = child.id
            }
        }

        override fun unregisterChild(child: ContainerContext) {
            mutableChildren.remove(child.id)
            if (activeChildId.value == child.id) {
                activeChildId.value = mutableChildren.values.firstOrNull {
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
            mutableChildren[child.id] = ChildState(child, isVisible)

            val currentlyActive = mutableChildren[activeChildId.value]
            if (currentlyActive == null || !currentlyActive.isVisible) {
                if (isVisible) {
                    activeChildId.value = child.id
                }
            }
            if (!isVisible && activeChildId.value == child.id) {
                activeChildId.value = mutableChildren.values.firstOrNull {
                    it.isVisible
                }?.child?.id
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
