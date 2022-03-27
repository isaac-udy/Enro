package dev.enro.core.fragment.container

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import dev.enro.core.NavigationContext
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.container.createEmptyBackStack
import dev.enro.core.navigationContext
import dev.enro.core.result.internal.ResultChannelImpl
import dev.enro.core.result.managedByLifecycle
import java.lang.ref.WeakReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


class FragmentNavigationContainerProperty @PublishedApi internal constructor(
    private val lifecycleOwner: LifecycleOwner,
    @IdRes private val containerId: Int,
    private val root: () -> NavigationKey?,
    private val navigationContext: () -> NavigationContext<*>,
    private val fragmentManager: () -> FragmentManager,
    private val emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    private val accept: (NavigationKey) -> Boolean
) : ReadOnlyProperty<Any, FragmentNavigationContainer> {

    private lateinit var navigationContainer: FragmentNavigationContainer

    init {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event != Lifecycle.Event.ON_CREATE) return

                val context = navigationContext()
                navigationContainer = FragmentNavigationContainer(
                    containerId = containerId,
                    parentContext = context,
                    accept = accept,
                    emptyBehavior = emptyBehavior,
                    fragmentManager = fragmentManager()
                )
                context.containerManager.addContainer(navigationContainer)
                val rootKey = root()
                rootKey?.let {
                    navigationContainer.setBackstack(
                        createEmptyBackStack().push(NavigationInstruction.Replace(rootKey), null)
                    )
                }
                lifecycleOwner.lifecycle.removeObserver(this)
            }
        })
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): FragmentNavigationContainer {
        return navigationContainer
    }
}

fun FragmentActivity.navigationContainer(
    @IdRes containerId: Int,
    root: () -> NavigationKey? = { null },
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean = { true },
): FragmentNavigationContainerProperty = FragmentNavigationContainerProperty(
    lifecycleOwner = this,
    containerId = containerId,
    root = root,
    navigationContext = { navigationContext },
    emptyBehavior = emptyBehavior,
    accept = accept,
    fragmentManager = { supportFragmentManager }
)

fun Fragment.navigationContainer(
    @IdRes containerId: Int,
    root: () -> NavigationKey? = { null },
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean = { true },
): FragmentNavigationContainerProperty = FragmentNavigationContainerProperty(
    lifecycleOwner = this,
    containerId = containerId,
    root = root,
    navigationContext = { navigationContext },
    emptyBehavior = emptyBehavior,
    accept = accept,
    fragmentManager = { childFragmentManager }
)