package dev.enro.core.result.internal

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dev.enro.core.NavigationHandle
import dev.enro.core.getNavigationHandle
import dev.enro.core.navigationContext
import dev.enro.core.result.EnroResult
import dev.enro.core.synthetic.SyntheticDestination
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@PublishedApi
internal class LazyResultChannelProperty<T>(
    owner: Any,
    resultType: Class<T>,
    onResult: (T) -> Unit
) : ReadOnlyProperty<Any, ResultChannelImpl<T>> {

    var resultChannel: ResultChannelImpl<T>? = null

    init {
        val handle = when (owner) {
            is FragmentActivity -> lazy { owner.getNavigationHandle() }
            is Fragment -> lazy { owner.getNavigationHandle() }
            is NavigationHandle -> lazy { owner as NavigationHandle }
            else -> throw IllegalArgumentException("Owner must be a Fragment, FragmentActivity, or NavigationHandle")
        }
        val lifecycle = owner as LifecycleOwner

        lifecycle.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_CREATE -> {
                        resultChannel = ResultChannelImpl(
                            navigationHandle = handle.value,
                            resultType = resultType,
                            onResult = onResult
                        )
                    }
                    Lifecycle.Event.ON_START -> {
                        EnroResult.from(handle.value.controller)
                            .apply {
                                registerChannel(resultChannel ?: return)
                            }
                    }
                    Lifecycle.Event.ON_STOP -> {
                        EnroResult.from(handle.value.controller)
                            .apply {
                                deregisterChannel(resultChannel ?: return)
                            }
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        lifecycle.lifecycle.removeObserver(this)
                        resultChannel = null
                    }
                    else -> {
                        // Do nothing
                    }
                }
            }
        })
    }

    override fun getValue(
        thisRef: Any,
        property: KProperty<*>
    ): ResultChannelImpl<T> = resultChannel!!
}
