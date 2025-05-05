package dev.enro.core

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.core.container.NavigationContainerManager
import dev.enro.core.controller.NavigationController
import dev.enro.core.internal.isDebugBuild


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
    private val getUnboundChildContext: () -> NavigationContext<*>? = { null },
    private val getContextInstruction: () -> AnyOpenInstruction?,
    private val getViewModelStoreOwner: () -> ViewModelStoreOwner,
    private val getSavedStateRegistryOwner: () -> SavedStateRegistryOwner,
    private val getLifecycleOwner: () -> LifecycleOwner,
    onBoundToNavigationHandle: NavigationContext<ContextType>.(NavigationHandle) -> Unit = {},
) {
    public val controller: NavigationController by lazy { getController() }
    public val parentContext: NavigationContext<*>? by lazy { getParentContext() }

    // TODO can we remove this or make it a strict mode thing?
    // Exists primarily for supporting Fragments/Activities that haven't been bound to a NavigationHandle yet
    internal val unboundChildContext: NavigationContext<*>? get() = getUnboundChildContext()

    private var onBoundToNavigationHandle: (NavigationContext<ContextType>.(NavigationHandle) -> Unit)? = onBoundToNavigationHandle

    /**
     * The contextInstruction is the NavigationInstruction that is included with the NavigationContext
     * as part of the ContextReference. For example, the NavigationInstruction that is passed to a Fragment
     * in the Fragment's arguments bundle, or the NavigationInstruction that is passed to an Activity
     * in the Intent's extras. This may be null, as not all NavigationContexts have a NavigationInstruction
     * associated with them by default, and if this is null, the NavigationHandle may include a
     * default open instruction, or may provide a NavigationInstruction for NoNavigationKey
     */
    internal val contextInstruction: AnyOpenInstruction? by lazy { getContextInstruction() }

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
        contextInstruction.hashCode()
        viewModelStoreOwner.hashCode()
        savedStateRegistryOwner.hashCode()
        lifecycleOwner.hashCode()

        val callback = requireNotNull(onBoundToNavigationHandle) {
            "This NavigationContext has already been bound to a NavigationHandle!"
        }
        onBoundToNavigationHandle = null
        callback(navigationHandle)
    }

    internal fun unbind(navigationHandle: NavigationHandle) {
        _navigationHandle = null
    }

    override fun toString(): String {
        return toString(formatted = isDebugBuild())
    }

    public fun toString(formatted: Boolean): String {
       if (!formatted) {
            return "NavigationContext(" +
                "contextReference=$contextReference," +
                "lifecycleState=${lifecycle.currentState}," +
                "instruction=$instruction," +
            ")"
        }
        val content = "contextReference=$contextReference,\n" +
                "lifecycleState=${lifecycle.currentState},\n" +
                "instruction=$instruction"
        return buildString {
            appendLine("NavigationContext(")
            content.lines().forEach {
                appendLine(it.prependIndent("    "))
            }
            append(")")
        }
    }
}

public fun NavigationContext<*>.toDisplayString(): String {
    val contextReferenceName = contextReference::class.qualifiedName ?: contextReference::class.toString()
    val navigationKeyName = instruction.navigationKey::class.qualifiedName ?: instruction.navigationKey::class.toString()
    return "NavigationContext<$contextReferenceName>($navigationKeyName)"
}
