package dev.enro.context

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
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

    protected val mutableChildren: SnapshotStateList<Child> = mutableStateListOf()
    public val children: List<Child> get() = mutableChildren

    public abstract val activeChild: Child?
    public abstract fun registerChild(child: Child)
    public abstract fun unregisterChild(child: Child)

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
}
