package dev.enro.compose

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.core.ComposableDestination
import dev.enro.core.NavigationContext
import dev.enro.core.OPEN_ARG
import dev.enro.core.activity
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.navigationController

internal class ComposeContext<ContextType : ComposableDestination>(
    contextReference: ContextType,
) : NavigationContext<ContextType>(contextReference) {
    override val controller: NavigationController get() = contextReference.parentContainer.parentContext.activity.application.navigationController
    override val parentContext: NavigationContext<*> get() = contextReference.parentContainer.parentContext
    override val lifecycle: Lifecycle get() = contextReference.lifecycle
    override val arguments: Bundle by lazy { bundleOf(OPEN_ARG to contextReference.instruction) }

    override val viewModelStoreOwner: ViewModelStoreOwner get() = contextReference
    override val savedStateRegistryOwner: SavedStateRegistryOwner get() = contextReference
    override val lifecycleOwner: LifecycleOwner get() = contextReference
}