package dev.enro.core.fragment.container

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dev.enro.core.*
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.asContainerRoot
import dev.enro.core.container.createEmptyBackStack
import dev.enro.core.navigationContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


class FragmentNavigationContainerProperty @PublishedApi internal constructor(
    private val lifecycleOwner: LifecycleOwner,
    @IdRes private val containerId: Int,
    private val root: () -> AnyOpenInstruction?,
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
                val rootInstruction = root()
                rootInstruction?.let {
                    navigationContainer.setBackstack(
                        createEmptyBackStack().push(
                            rootInstruction.asContainerRoot()
                        )
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
    root = {
        NavigationInstruction.DefaultDirection(
            root() ?: return@FragmentNavigationContainerProperty null
        )
    },
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
    root = {
        NavigationInstruction.DefaultDirection(
            root() ?: return@FragmentNavigationContainerProperty null
        )
    },
    navigationContext = { navigationContext },
    emptyBehavior = emptyBehavior,
    accept = accept,
    fragmentManager = { childFragmentManager }
)