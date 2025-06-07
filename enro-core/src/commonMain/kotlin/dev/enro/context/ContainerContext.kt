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
        val childrenById = mutableChildren.associateBy { it.id }
        val backstack = container.backstack
        for (index in container.backstack.indices.reversed()) {
            val instance = backstack[index]
            childrenById[instance.id]?.let { return@derivedStateOf it }
        }
        return@derivedStateOf null
    }

    override fun registerChild(child: DestinationContext<NavigationKey>) {
        mutableChildren.add(child)
    }

    override fun unregisterChild(child: DestinationContext<NavigationKey>) {
        mutableChildren.remove(child)
    }
}