package dev.enro.core.result.internal

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dev.enro.core.NavigationHandle
import dev.enro.core.getNavigationHandle
import dev.enro.core.result.EnroResult
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@PublishedApi
internal class LazyResultChannelProperty<T>(
    private val owner: Any,
    private val resultType: Class<T>,
    private val onResult: (T) -> Unit
) : ReadOnlyProperty<Any, ResultChannelImpl<T>> {

    lateinit var resultChannel: ResultChannelImpl<T>

    init {
        val handle = when (owner) {
                is FragmentActivity -> lazy { owner.getNavigationHandle() }
                is Fragment ->lazy { owner.getNavigationHandle() }
                is NavigationHandle -> lazy { owner as NavigationHandle }
                else -> throw IllegalArgumentException("Owner must be a Fragment, FragmentActivity, or NavigationHandle")
            }
        val lifecycle = owner as LifecycleOwner

        lifecycle.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if(event != Lifecycle.Event.ON_START)  return
                lifecycle.lifecycle.removeObserver(this)

                resultChannel = ResultChannelImpl(
                    navigationHandle = handle.value,
                    resultType = resultType,
                    onResult = onResult
                )
            }
        })

        lifecycle.lifecycle.addObserver(object: LifecycleEventObserver {
            private var enroResult: EnroResult? = null
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if(event == Lifecycle.Event.ON_STOP) {
                    enroResult = EnroResult.from(handle.value.controller)
                }
                if(event == Lifecycle.Event.ON_DESTROY) {
                    enroResult?.deregisterChannel(resultChannel)
                    enroResult = null
                }
            }
        })
    }

    override fun getValue(
        thisRef: Any,
        property: KProperty<*>
    ): ResultChannelImpl<T> = resultChannel
}