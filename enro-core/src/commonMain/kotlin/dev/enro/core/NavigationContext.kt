package dev.enro.core

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedState
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.controller.NavigationController
import dev.enro.core.container.NavigationContainerManager


/**
 * NavigationContext represents a context in which navigation can occur. In Android, this may be a Fragment, Activity, or Composable.
 *
 * When constructing a NavigationContext, the contextReference is the actual object that the NavigationContext represents
 * (e.g. a Fragment, Activity or Composable), and the other parameters are functions that can be used to retrieve information
 * about the context. The get functions are invoked lazily, either when the are accessed for the first time,
 * or once the NavigationContext is bound to a NavigationHandle.
 */
public class NavigationContext<ContextType : Any> internal constructor(
    public val contextReference: ContextType,
    private val getController: () -> NavigationController,
    private val getParentContext: () -> NavigationContext<*>?,
    private val getArguments: () -> SavedState,
    private val getViewModelStoreOwner: () -> ViewModelStoreOwner,
    private val getSavedStateRegistryOwner: () -> SavedStateRegistryOwner,
    private val getLifecycleOwner: () -> LifecycleOwner,
    onBoundToNavigationHandle: NavigationContext<ContextType>.(NavigationHandle) -> Unit = {},
) {
    public val controller: NavigationController by lazy { getController() }
    public val parentContext: NavigationContext<*>? by lazy { getParentContext() }
    private var onBoundToNavigationHandle: (NavigationContext<ContextType>.(NavigationHandle) -> Unit)? = onBoundToNavigationHandle

    /**
     * The arguments provided to this NavigationContext. It is possible to read the open instruction from these arguments,
     * but it may be different than the open instruction attached to the NavigationHandle. If the arguments do not contain
     * a NavigationInstruction, a NavigationInstruction is still provided to the NavigationHandle, which will be either a
     * default key (if one is provided with the destination) or a "NoNavigationKey" NavigationKey.
     *
     * Generally it should be preferred to read the instruction property, rather than read the instruction from the arguments.
     */
    @AdvancedEnroApi
    public val arguments: SavedState by lazy { getArguments() }

    private lateinit var _instruction: NavigationInstruction.Open<*>
    public val instruction: NavigationInstruction.Open<*> get() = _instruction

    public val viewModelStoreOwner: ViewModelStoreOwner by lazy { getViewModelStoreOwner() }
    public val savedStateRegistryOwner: SavedStateRegistryOwner by lazy { getSavedStateRegistryOwner() }
    public val lifecycleOwner: LifecycleOwner by lazy { getLifecycleOwner() }
    public val lifecycle: Lifecycle get() = lifecycleOwner.lifecycle

    public val containerManager: NavigationContainerManager = NavigationContainerManager()

    private var _navigationHandle: NavigationHandle? = null
    public val navigationHandle: NavigationHandle get() = requireNotNull(_navigationHandle)

    internal fun bind(navigationHandle: NavigationHandle) {
        _navigationHandle = navigationHandle
        _instruction = navigationHandle.instruction

        // Invoke hashcode on all lazy items to ensure they are initialized

        controller.hashCode()
        parentContext.hashCode()
        arguments.hashCode()
        viewModelStoreOwner.hashCode()
        savedStateRegistryOwner.hashCode()
        lifecycleOwner.hashCode()

        val callback = requireNotNull(onBoundToNavigationHandle) {
            "This NavigationContext has already been bound to a NavigationHandle!"
        }
        onBoundToNavigationHandle = null
        callback(navigationHandle)
    }
}