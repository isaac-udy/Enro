package dev.enro.core.container

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

public class NavigationContainerProperty<T : NavigationContainer> @PublishedApi internal constructor(
    private val lifecycleOwner: LifecycleOwner,
    navigationContainerProducer: () -> T,
    private val onContainerAttached: (T) -> Unit = {},
) : ReadOnlyProperty<Any, T> {

    internal val navigationContainer: T by lazy {
        navigationContainerProducer()
            .also {
                it.context.containerManager.addContainer(it)
                onContainerAttached(it)
            }
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

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return navigationContainer
    }
}