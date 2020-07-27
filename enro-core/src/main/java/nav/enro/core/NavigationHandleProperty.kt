package nav.enro.core

import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import nav.enro.core.context.ChildContainer
import nav.enro.core.internal.handle.NavigationHandleViewModel
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


class NavigationHandleProperty<Key : NavigationKey> @PublishedApi internal constructor(
    private val lifecycleOwner: LifecycleOwner,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val configBuilder: NavigationHandleConfiguration<Key>.() -> Unit = {}
) : ReadOnlyProperty<Any, NavigationHandle<Key>> {

    private val config = NavigationHandleConfiguration<Key>().apply(configBuilder)

    private val navigationHandle by lazy {
        val navigationHandle = ViewModelProvider(viewModelStoreOwner, ViewModelProvider.NewInstanceFactory())
            .get(NavigationHandleViewModel::class.java)
                as NavigationHandleViewModel<Key>

        config.applyTo(navigationHandle)

        return@lazy navigationHandle
    }

    init {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if(event == Lifecycle.Event.ON_CREATE) {
                    navigationHandle.hashCode()
                }
            }
        })
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): NavigationHandle<Key> {
        return navigationHandle
    }
}

class NavigationHandleConfiguration<T : NavigationKey> @PublishedApi internal constructor() {

    private var childContainers = listOf<ChildContainer>()

    fun container(@IdRes containerId: Int, accept: (NavigationKey) -> Boolean = { true }) {
        childContainers = childContainers + ChildContainer(containerId, accept)
    }

    internal fun applyTo(navigationHandleViewModel: NavigationHandleViewModel<T>) {
        navigationHandleViewModel.childContainers = childContainers
    }
}


fun <T : NavigationKey> FragmentActivity.navigationHandle(
    config: NavigationHandleConfiguration<T>.() -> Unit = {}
): NavigationHandleProperty<T> = NavigationHandleProperty(
    lifecycleOwner = this,
    viewModelStoreOwner = this,
    configBuilder = config
)

fun <T : NavigationKey> Fragment.navigationHandle(
    config: NavigationHandleConfiguration<T>.() -> Unit = {}
): NavigationHandleProperty<T> = NavigationHandleProperty(
    lifecycleOwner = this,
    viewModelStoreOwner = this,
    configBuilder = config
)

fun <T : NavigationKey> FragmentActivity.getNavigationHandle(): NavigationHandle<T> =
    viewModels<NavigationHandleViewModel<T>> { ViewModelProvider.NewInstanceFactory() } .value

fun <T : NavigationKey> Fragment.getNavigationHandle(): NavigationHandle<T> =
    viewModels<NavigationHandleViewModel<T>> { ViewModelProvider.NewInstanceFactory() } .value