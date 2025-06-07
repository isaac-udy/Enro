package dev.enro.context

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.EnroController

public class RootContext(
    parent: Any,
    override val controller: EnroController,
    lifecycleOwner: LifecycleOwner,
    viewModelStoreOwner: ViewModelStoreOwner,
    defaultViewModelProviderFactory: HasDefaultViewModelProviderFactory,
    private val activeChildId: MutableState<String?>,
) : NavigationContext<Any, ContainerContext>(),
    LifecycleOwner by lifecycleOwner,
    ViewModelStoreOwner by viewModelStoreOwner,
    HasDefaultViewModelProviderFactory by defaultViewModelProviderFactory {

    override val id: String = "Root"
    override val parent: Any = parent

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