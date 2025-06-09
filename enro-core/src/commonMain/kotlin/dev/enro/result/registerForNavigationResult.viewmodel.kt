package dev.enro.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.enro.NavigationKey
import dev.enro.getNavigationHandle
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass


public inline fun <reified R : Any> ViewModel.registerForNavigationResult(
    noinline onClosed: NavigationResultScope<out NavigationKey.WithResult<out R>>.() -> Unit = {},
    noinline onCompleted: NavigationResultScope<out NavigationKey.WithResult<out R>>.(R) -> Unit,
): ReadOnlyProperty<ViewModel, NavigationResultChannel<R>> {
    return registerForNavigationResult(
        resultType = R::class,
        onClosed = onClosed,
        onCompleted = onCompleted,
    )
}

public fun <R : Any> ViewModel.registerForNavigationResult(
    resultType: KClass<R>,
    onClosed: NavigationResultScope<NavigationKey.WithResult<R>>.() -> Unit = {},
    onCompleted: NavigationResultScope<NavigationKey.WithResult<R>>.(R) -> Unit,
): ReadOnlyProperty<ViewModel, NavigationResultChannel<R>> {
    val navigation = getNavigationHandle()
    val scope = viewModelScope
    @Suppress("UNCHECKED_CAST")
    val channel = NavigationResultChannel<R>(
        id = NavigationResultChannel.Id(
            ownerId = navigation.id,
            resultId = onClosed::class.qualifiedName + onCompleted::class.qualifiedName,
        ),
        onClosed = {
            this as NavigationResultScope<NavigationKey.WithResult<R>>
            onClosed()
        },
        onCompleted = {
            this as NavigationResultScope<NavigationKey.WithResult<R>>
            onCompleted(it)
        },
        navigationHandle = navigation,
    )
    NavigationResultChannel.observe(resultType, scope, channel)
    return ReadOnlyProperty { vm, _ ->
        require(vm === this)
        channel
    }
}

public fun ViewModel.registerForNavigationResult(
    onClosed: NavigationResultScope<out NavigationKey>.() -> Unit = {},
    onCompleted: NavigationResultScope<out NavigationKey>.() -> Unit,
): ReadOnlyProperty<ViewModel, NavigationResultChannel<Unit>> {
    val navigation = getNavigationHandle()
    val scope = viewModelScope
    @Suppress("UNCHECKED_CAST")
    val channel = NavigationResultChannel<Unit>(
        id = NavigationResultChannel.Id(
            ownerId = navigation.id,
            resultId = onClosed::class.qualifiedName + onCompleted::class.qualifiedName,
        ),
        onClosed = {
            onClosed()
        },
        onCompleted = {
            onCompleted()
        },
        navigationHandle = navigation,
    )
    NavigationResultChannel.observe(Unit::class, scope, channel)
    return ReadOnlyProperty { vm, _ ->
        require(vm === this)
        channel
    }
}