package dev.enro.context

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass

/**
 * When attempting to find a ViewModel in a NavigationContext, we don't want to create a new ViewModel, rather we want to
 * get an existing instance of that ViewModel, if it exists, so this ViewModelProvider.Factory always throws an exception
 * if it is ever asked to actually create a ViewModel.
 */
private class NavigationContextViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        modelClass: KClass<T>,
        extras: CreationExtras,
    ): T {
        error("Failed to create ViewModel $modelClass. This factory should not be used to create ViewModels.")
    }
}

private fun viewModelNotFoundError(context: AnyNavigationContext, modelClass: KClass<*>): Nothing {
    val contextString = when (context) {
        is DestinationContext<*> -> "NavigationContext.Destination with key: ${context.key::class.simpleName} and id: ${context.id}"
        is ContainerContext -> "NavigationContext.Container with id: ${context.id}"
        is RootContext -> "NavigationContext.Root"
    }
    error("ViewModel ${modelClass.simpleName} was not found in $contextString")
}

/**
 * Attempt to get a ViewModel of a certain type from a NavigationContext.
 *
 * @return The ViewModel requested, or null if the ViewModel does not exist in the NavigationContext's ViewModelStore
 */
public fun <T : ViewModel> AnyNavigationContext.getViewModel(
    cls: KClass<T>,
    key: String? = null,
): T? {
    val provider = ViewModelProvider.create(
        owner = this,
        factory = NavigationContextViewModelFactory(),
    )
    val result = kotlin.runCatching {
        if (key == null) {
            provider.get(modelClass = cls)
        } else {
            provider.get(modelClass = cls, key = key)
        }
    }
    return result.getOrNull()
}

/**
 * Attempt to get a ViewModel of a certain type from a NavigationContext.
 *
 * @return The ViewModel requested
 *
 * @throws IllegalStateException if the ViewModel does not already exist in the NavigationContext
 */
public fun <T : ViewModel> AnyNavigationContext.requireViewModel(
    cls: KClass<T>,
    key: String? = null,
): T {
    return getViewModel(cls, key)
        ?: viewModelNotFoundError(this, cls)
}

/**
 * Attempt to get a ViewModel of a certain type from a NavigationContext.
 *
 * @return The ViewModel requested, or null if the ViewModel does not exist in the NavigationContext's ViewModelStore
 */
public inline fun <reified T : ViewModel> AnyNavigationContext.getViewModel(
    key: String? = null,
): T? {
    return getViewModel(T::class, key)
}

/**
 * Attempt to get a ViewModel of a certain type from a NavigationContext.
 *
 * @return The ViewModel requested
 *
 * @throws IllegalStateException if the ViewModel does not already exist in the NavigationContext
 */
public inline fun <reified T : ViewModel> AnyNavigationContext.requireViewModel(
    key: String? = null,
): T {
    return requireViewModel(T::class, key)
}