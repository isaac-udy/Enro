package dev.enro.context

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.EnroController
import dev.enro.NavigationContainer
import dev.enro.NavigationKey

public class ContainerContext(
    override val parent: NavigationContext<*, ContainerContext>,
    public val container: NavigationContainer,
) : NavigationContext<NavigationContext<*, ContainerContext>, DestinationContext<NavigationKey>>(),
    LifecycleOwner by parent,
    ViewModelStoreOwner by parent,
    HasDefaultViewModelProviderFactory by parent {

    override val id: String = container.key.name
    override val controller: EnroController = parent.controller

    override val activeChild: DestinationContext<NavigationKey>? by derivedStateOf {
        val backstack = container.backstack
        for (index in container.backstack.indices.reversed()) {
            val instance = backstack[index]
            mutableChildren[instance.id]
                ?.takeIf { it.isVisible }
                ?.let { return@derivedStateOf it.child }
        }
        return@derivedStateOf null
    }

    override fun registerChild(child: DestinationContext<NavigationKey>) {
        mutableChildren[child.id] = ChildState(child, false)
    }

    override fun unregisterChild(child: DestinationContext<NavigationKey>) {
        mutableChildren.remove(child.id)
    }

    override fun registerVisibility(
        child: DestinationContext<NavigationKey>,
        isVisible: Boolean,
    ) {
        val current = mutableChildren[child.id]
        if (current == null) return
        if (current.isVisible == isVisible) return
        mutableChildren[child.id] = ChildState(child, isVisible)
    }
}