package dev.enro.core.result.internal

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dev.enro.core.EnroLifecycleException
import dev.enro.core.NavigationHandle
import dev.enro.core.getNavigationHandle
import dev.enro.core.result.EnroResultChannel
import dev.enro.core.result.managedByLifecycle
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@PublishedApi
internal class LazyResultChannelProperty<T>(
    owner: Any,
    resultType: Class<T>,
    onResult: (T) -> Unit
) : ReadOnlyProperty<Any, EnroResultChannel<T>> {

    private var resultChannel: EnroResultChannel<T>? = null

    init {
        val handle = when (owner) {
            is FragmentActivity -> lazy { owner.getNavigationHandle() }
            is Fragment -> lazy { owner.getNavigationHandle() }
            is NavigationHandle -> lazy { owner as NavigationHandle }
            else -> throw IllegalArgumentException("Owner must be a Fragment, FragmentActivity, or NavigationHandle")
        }
        val lifecycleOwner = owner as LifecycleOwner
        val lifecycle = lifecycleOwner.lifecycle

        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event != Lifecycle.Event.ON_CREATE) return;
                resultChannel = ResultChannelImpl(
                    navigationHandle = handle.value,
                    resultType = resultType,
                    onResult = onResult
                ).managedByLifecycle(lifecycle)
            }
        })
    }

    override fun getValue(
        thisRef: Any,
        property: KProperty<*>
    ): EnroResultChannel<T> = resultChannel ?: throw EnroLifecycleException(
        "LazyResultChannelProperty's EnroResultChannel is not initialised. Are you attempting to use the result channel before the result channel's lifecycle owner has entered the CREATED state?"
    )
}
