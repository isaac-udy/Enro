package dev.enro.core.fragment.container

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.asPushInstruction
import dev.enro.core.container.createEmptyBackStack
import dev.enro.core.navigationContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


class FragmentNavigationContainerProperty @PublishedApi internal constructor(
    private val lifecycleOwner: LifecycleOwner,
    @IdRes private val containerId: Int,
    private val root: () -> AnyOpenInstruction?,
    private val navigationContext: () -> NavigationContext<*>,
    private val emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    private val accept: (NavigationKey) -> Boolean
) : ReadOnlyProperty<Any, FragmentNavigationContainer> {

    private val navigationContainer: FragmentNavigationContainer by lazy {
        val context = navigationContext()
        val container = FragmentNavigationContainer(
            containerId = containerId,
            parentContext = context,
            accept = accept,
            emptyBehavior = emptyBehavior
        )
        context.containerManager.addContainer(container)
        val rootInstruction = root()
        rootInstruction?.let {
            container.setBackstack(
                createEmptyBackStack().push(
                    rootInstruction.asPushInstruction()
                )
            )
        }

        return@lazy container
    }

    init {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event != Lifecycle.Event.ON_CREATE) return
                // reference the navigation container directly so it is created
                navigationContainer.hashCode()
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
    accept = accept
)

@JvmName("navigationContainerFromInstruction")
fun FragmentActivity.navigationContainer(
    @IdRes containerId: Int,
    rootInstruction: () -> NavigationInstruction.Open<*>?,
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean = { true },
): FragmentNavigationContainerProperty = FragmentNavigationContainerProperty(
    lifecycleOwner = this,
    containerId = containerId,
    root = rootInstruction,
    navigationContext = { navigationContext },
    emptyBehavior = emptyBehavior,
    accept = accept
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
    accept = accept
)

@JvmName("navigationContainerFromInstruction")
fun Fragment.navigationContainer(
    @IdRes containerId: Int,
    rootInstruction: () -> NavigationInstruction.Open<*>?,
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean = { true },
): FragmentNavigationContainerProperty = FragmentNavigationContainerProperty(
    lifecycleOwner = this,
    containerId = containerId,
    root = rootInstruction,
    navigationContext = { navigationContext },
    emptyBehavior = emptyBehavior,
    accept = accept
)