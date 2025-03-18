package dev.enro.core.result.internal

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import dev.enro.core.EnroException
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.controller.usecase.createResultChannel
import dev.enro.core.getNavigationHandle
import dev.enro.core.result.NavigationResultChannel
import dev.enro.core.result.NavigationResultScope
import dev.enro.core.result.managedByLifecycle
import dev.enro.viewmodel.getNavigationHandle
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@PublishedApi
internal class LazyResultChannelProperty<Owner : Any, Result : Any, Key : NavigationKey.WithResult<Result>>(
    owner: Any,
    resultId: String,
    resultType: KClass<Result>,
    onClosed: NavigationResultScope<Result, Key>.() -> Unit = {},
    onResult: NavigationResultScope<Result, Key>.(Result) -> Unit,
    additionalResultId: String = "",
) : ReadOnlyProperty<Owner, NavigationResultChannel<Result, Key>> {

    private var resultChannel: NavigationResultChannel<Result, Key>? = null

    init {
        val handle = when (owner) {
            is ComponentActivity -> lazy { owner.getNavigationHandle() }
            is Fragment -> lazy { owner.getNavigationHandle() }
            is NavigationHandle -> lazy { owner as NavigationHandle }
            is ViewModel -> lazy { owner.getNavigationHandle() }
            else -> throw EnroException.UnreachableState()
        }
        val lifecycleOwner: LifecycleOwner = when (owner) {
            is LifecycleOwner -> owner
            is ViewModel -> owner.getNavigationHandle()
            else -> error("Can't find LifecycleOwner from $owner")
        }
        val lifecycle = lifecycleOwner.lifecycle

        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event != Lifecycle.Event.ON_CREATE) return;
                resultChannel = handle.value.createResultChannel(
                    resultType = resultType,
                    resultId = resultId,
                    onClosed = onClosed,
                    onResult = onResult,
                    additionalResultId = additionalResultId,
                ).managedByLifecycle(lifecycle)
            }
        })
    }

    override fun getValue(
        thisRef: Owner,
        property: KProperty<*>
    ): NavigationResultChannel<Result, Key> = resultChannel ?: throw EnroException.ResultChannelIsNotInitialised(
        "LazyResultChannelProperty's EnroResultChannel is not initialised. Are you attempting to use the result channel before the result channel's lifecycle owner has entered the CREATED state?"
    )
}
