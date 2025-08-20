package dev.enro.core.result

import androidx.lifecycle.ViewModel
import dev.enro.NavigationKey
import dev.enro.result.NavigationResultScope
import kotlin.properties.ReadOnlyProperty
import dev.enro.result.registerForNavigationResult as realRegisterForNavigationResult

public inline fun <reified R : Any> ViewModel.registerForNavigationResult(
    noinline onClosed: NavigationResultScope<out NavigationKey>.() -> Unit = {},
    noinline onResult: NavigationResultScope<out NavigationKey>.(R) -> Unit,
) : ReadOnlyProperty<ViewModel, NavigationResultChannel<R, *>> {
    val channel = realRegisterForNavigationResult(
        onClosed = onClosed,
        onCompleted = onResult,
    )
    return ReadOnlyProperty<ViewModel, NavigationResultChannel<R, *>> { viewModel, property ->
        NavigationResultChannel<R, dev.enro.core.NavigationKey>(
            wrapped = channel.provideDelegate(viewModel, property).getValue(viewModel, property)
        )
    }
}

