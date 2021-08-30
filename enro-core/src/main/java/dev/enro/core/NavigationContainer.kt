package dev.enro.core

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.core.compose.AbstractComposeFragmentHostKey
import dev.enro.core.compose.EnroContainerController
import java.lang.ref.WeakReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

sealed class EmptyBehavior {
    /**
     * When this container is about to become empty, allow this container to become empty
     */
    object AllowEmpty : EmptyBehavior()

    /**
     * When this container is about to become empty, do not close the NavigationDestination in the
     * container, but instead close the parent NavigationDestination (i.e. the owner of this container)
     */
    object CloseParent : EmptyBehavior()

    /**
     * When this container is about to become empty, execute an action. If the result of the action function is
     * "true", then the action is considered to have consumed the request to become empty, and the container
     * will not close the last navigation destination. When the action function returns "false", the default
     * behaviour will happen, and the container will become empty.
     */
    class Action(
            val onEmpty: () -> Boolean
    ) : EmptyBehavior()
}

class NavigationContainer internal constructor(
    @IdRes val containerId: Int,
    private val root: NavigationKey? = null,
    val emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    private val accept: (NavigationKey) -> Boolean
) {
    fun accept(key: NavigationKey): Boolean {
        if (key is AbstractComposeFragmentHostKey && accept.invoke(key.instruction.navigationKey)) return true
        return accept.invoke(key)
    }

    internal fun openRoot(navigationHandle: NavigationHandle) {
        if (root == null) return
        navigationHandle.executeInstruction(
            NavigationInstruction.Forward(root)
                .setTargetContainer(containerId)
        )
    }
}

class NavigationContainerProperty @PublishedApi internal constructor(
    private val lifecycleOwner: LifecycleOwner,
    private val navigationContainer: NavigationContainer
) : ReadOnlyProperty<Any, NavigationContainer> {

    init {
        pendingContainers.getOrPut(lifecycleOwner.hashCode()) { mutableListOf() }
            .add(WeakReference(navigationContainer))
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): NavigationContainer {
        return navigationContainer
    }

    companion object {
        private val pendingContainers =
            mutableMapOf<Int, MutableList<WeakReference<NavigationContainer>>>()

        internal fun getPendingContainers(lifecycleOwner: LifecycleOwner): List<NavigationContainer> {
            val pending = pendingContainers[lifecycleOwner.hashCode()] ?: return emptyList()
            val containers = pending.mapNotNull { it.get() }
            pendingContainers.remove(lifecycleOwner.hashCode())
            return containers
        }
    }
}

fun FragmentActivity.navigationContainer(
    @IdRes containerId: Int,
    root: NavigationKey? = null,
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean
): NavigationContainerProperty = NavigationContainerProperty(
    this,
    NavigationContainer(
        containerId = containerId,
        root = root,
        emptyBehavior = emptyBehavior,
        accept = accept,
    )
)

fun Fragment.navigationContainer(
    @IdRes containerId: Int,
    root: NavigationKey? = null,
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean
): NavigationContainerProperty = NavigationContainerProperty(
    this,
    NavigationContainer(
        containerId = containerId,
        root = root,
        emptyBehavior = emptyBehavior,
        accept = accept,
    )
)