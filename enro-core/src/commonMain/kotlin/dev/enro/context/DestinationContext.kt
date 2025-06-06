package dev.enro.context

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination

public class DestinationContext<T : NavigationKey>(
    lifecycleOwner: LifecycleOwner,
    viewModelStoreOwner: ViewModelStoreOwner,
    defaultViewModelProviderFactory: HasDefaultViewModelProviderFactory,
    public override val parent: ContainerContext,
    public val destination: NavigationDestination<T>,
    private val activeChildId: MutableState<String?>,
) : NavigationContext<ContainerContext, ContainerContext>(),
    LifecycleOwner by lifecycleOwner,
    ViewModelStoreOwner by viewModelStoreOwner,
    HasDefaultViewModelProviderFactory by defaultViewModelProviderFactory  {

    override val id: String get() = destination.id
    public val key: T get() = destination.key
    public val instance: NavigationKey.Instance<T> get() = destination.instance

    override val activeChild: ContainerContext? by derivedStateOf {
        children.firstOrNull { it.id == activeChildId.value }
    }

    override fun registerChild(child: ContainerContext) {
        mutableChildren.add(child)
        if (activeChildId.value == null) {
            activeChildId.value = child.id
        }
    }

    override fun unregisterChild(child: ContainerContext) {
        mutableChildren.remove(child)
        if (activeChildId.value == child.id) {
            activeChildId.value = mutableChildren.firstOrNull()?.id
        }
    }

    public fun setActiveContainer(childId: String) {
        val child = children.firstOrNull { it.id == childId }
        if (child != null) {
            activeChildId.value = child.id
        }
    }
}