package dev.enro.context

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass

// getViewModel returns an existing ViewModel and never creates one, so this factory throws
// when the ViewModelProvider calls `create`.
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
 * Returns the existing ViewModel of type [cls] in the ViewModelStore of this context, or null
 * if the ViewModelStore has no ViewModel of type [cls]. [getViewModel] never creates a ViewModel.
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
 * Returns the existing ViewModel of type [cls] in the ViewModelStore of this context.
 *
 * @throws IllegalStateException if the ViewModelStore has no ViewModel of type [cls]
 */
public fun <T : ViewModel> AnyNavigationContext.requireViewModel(
    cls: KClass<T>,
    key: String? = null,
): T {
    return getViewModel(cls, key)
        ?: viewModelNotFoundError(this, cls)
}

/**
 * Returns the existing [T] in the ViewModelStore of this context, or null if the
 * ViewModelStore has no [T]. [getViewModel] never creates a ViewModel.
 */
public inline fun <reified T : ViewModel> AnyNavigationContext.getViewModel(
    key: String? = null,
): T? {
    return getViewModel(T::class, key)
}

/**
 * Returns the existing [T] in the ViewModelStore of this context.
 *
 * @throws IllegalStateException if the ViewModelStore has no [T]
 */
public inline fun <reified T : ViewModel> AnyNavigationContext.requireViewModel(
    key: String? = null,
): T {
    return requireViewModel(T::class, key)
}