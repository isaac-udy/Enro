package dev.enro.result

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import dev.enro.NavigationKey
import dev.enro.platform.getNavigationKeyInstance
import dev.enro.ui.destinations.fragment.fragmentContextHolder
import kotlinx.coroutines.Job
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass


public inline fun <reified R : Any> Fragment.registerForNavigationResult(
    noinline onClosed: NavigationResultScope<out NavigationKey.WithResult<R>>.() -> Unit = {},
    noinline onCompleted: NavigationResultScope<out NavigationKey.WithResult<R>>.(R) -> Unit,
): ReadOnlyProperty<Fragment, NavigationResultChannel<R>> {
    return registerForNavigationResult(R::class, onClosed, onCompleted)
}

public fun <R : Any> Fragment.registerForNavigationResult(
    @Suppress("unused")
    resultType: KClass<R>,
    onClosed: NavigationResultScope<out NavigationKey.WithResult<R>>.() -> Unit = {},
    onCompleted: NavigationResultScope<out NavigationKey.WithResult<R>>.(R) -> Unit,
): ReadOnlyProperty<Fragment, NavigationResultChannel<R>> {
    val lazyResultChannel = lazy {
        val id = arguments?.getNavigationKeyInstance()?.id ?: TODO()
        NavigationResultChannel<R>(
            id = NavigationResultChannel.Id(
                ownerId = id,
                resultId = onCompleted::class.java.name,
            ),
            onClosed = onClosed as NavigationResultScope<NavigationKey>.() -> Unit,
            onCompleted = onCompleted as NavigationResultScope<NavigationKey>.(R) -> Unit,
            navigationHandle = fragmentContextHolder.navigationHandle,
        )
    }
    var job: Job? = null
    lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            job = NavigationResultChannel.observe(resultType, lifecycleScope, lazyResultChannel.value)
        }
        if (event == Lifecycle.Event.ON_PAUSE) {
            job?.cancel()
        }
    })
    return ReadOnlyProperty<Fragment, NavigationResultChannel<R>> { _, _ ->
        lazyResultChannel.value
    }
}