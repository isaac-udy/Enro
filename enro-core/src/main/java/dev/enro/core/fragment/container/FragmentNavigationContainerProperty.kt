package dev.enro.core.fragment.container

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.container.createEmptyBackStack
import dev.enro.core.navigationContext
import java.lang.ref.WeakReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


class FragmentNavigationContainerProperty @PublishedApi internal constructor(
    private val lifecycleOwner: LifecycleOwner,
    private val root: () -> NavigationKey?,
    private val navigationContainer: FragmentNavigationContainer
) : ReadOnlyProperty<Any, FragmentNavigationContainer> {

    init {
        lifecycleOwner.lifecycleScope.launchWhenCreated {
            val rootKey = root() ?: return@launchWhenCreated
            navigationContainer.setBackstack(
                createEmptyBackStack().push(NavigationInstruction.Replace(rootKey), null)
            )
        }
        pendingContainers.getOrPut(lifecycleOwner.hashCode()) { mutableListOf() }
            .add(WeakReference(navigationContainer))
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): FragmentNavigationContainer {
        return navigationContainer
    }

    companion object {
        private val pendingContainers =
            mutableMapOf<Int, MutableList<WeakReference<FragmentNavigationContainer>>>()

        internal fun getPendingContainers(lifecycleOwner: LifecycleOwner): List<FragmentNavigationContainer> {
            val pending = pendingContainers[lifecycleOwner.hashCode()] ?: return emptyList()
            val containers = pending.mapNotNull { it.get() }
            pendingContainers.remove(lifecycleOwner.hashCode())
            return containers
        }
    }
}

fun FragmentActivity.navigationContainer(
    @IdRes containerId: Int,
    root: () -> NavigationKey? = { null },
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean = { true },
): FragmentNavigationContainerProperty = FragmentNavigationContainerProperty(
    this,
    root,
    FragmentNavigationContainer(
        parentContextFactory = { navigationContext },
        containerId = containerId,
        emptyBehavior = emptyBehavior,
        accept = accept,
        fragmentManager = { supportFragmentManager }
    )
)

fun Fragment.navigationContainer(
    @IdRes containerId: Int,
    root: () -> NavigationKey? = { null },
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean = { true },
): FragmentNavigationContainerProperty = FragmentNavigationContainerProperty(
    this,
    root,
    FragmentNavigationContainer(
        containerId = containerId,
        parentContextFactory = { navigationContext },
        emptyBehavior = emptyBehavior,
        accept = accept,
        fragmentManager = { childFragmentManager }
    )
)