package dev.enro.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass

/**
 * When attempting to find a ViewModel in a NavigationContext, we don't want to create a new ViewModel, rather we want to
 * get an existing instance of that ViewModel, if it exists, so this ViewModelProvider.Factory always throws an exception
 * if it is ever asked to actually create a ViewModel.
 */
private class NavigationContextViewModelFactory(
    private val context: NavigationContext<*>,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        viewModelNotFoundError(context, modelClass)
    }

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        viewModelNotFoundError(context, modelClass)
    }
}

private fun viewModelNotFoundError(context: NavigationContext<*>, modelClass: Class<*>): Nothing {
    val key = context.instruction.navigationKey
    error("ViewModel ${modelClass.simpleName} was not found in NavigationContext with navigation key $key")
}

/**
 * Attempt to get a ViewModel of a certain type from a NavigationContext.
 *
 * @return The ViewModel requested, or null if the ViewModel does not exist in the NavigationContext's ViewModelStore
 */
public fun <T : ViewModel> NavigationContext<*>.getViewModel(
    cls: Class<T>,
    key: String? = null,
): T? {
    val provider = ViewModelProvider(
        store = viewModelStoreOwner.viewModelStore,
        factory = NavigationContextViewModelFactory(this)
    )
    val result = kotlin.runCatching {
        when (key) {
            null -> provider[cls]
            else -> provider[key, cls]
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
public fun <T : ViewModel> NavigationContext<*>.requireViewModel(
    cls: Class<T>,
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
public fun <T : ViewModel> NavigationContext<*>.getViewModel(
    cls: KClass<T>,
    key: String? = null,
): T? {
    return getViewModel(cls.java, key)
}

/**
 * Attempt to get a ViewModel of a certain type from a NavigationContext.
 *
 * @return The ViewModel requested
 *
 * @throws IllegalStateException if the ViewModel does not already exist in the NavigationContext
 */
public fun <T : ViewModel> NavigationContext<*>.requireViewModel(
    cls: KClass<T>,
    key: String? = null,
): T {
    return requireViewModel(cls.java, key)
}

/**
 * Attempt to get a ViewModel of a certain type from a NavigationContext.
 *
 * @return The ViewModel requested, or null if the ViewModel does not exist in the NavigationContext's ViewModelStore
 */
public inline fun <reified T : ViewModel> NavigationContext<*>.getViewModel(
    key: String? = null,
): T? {
    return getViewModel(T::class.java, key)
}

/**
 * Attempt to get a ViewModel of a certain type from a NavigationContext.
 *
 * @return The ViewModel requested
 *
 * @throws IllegalStateException if the ViewModel does not already exist in the NavigationContext
 */
public inline fun <reified T : ViewModel> NavigationContext<*>.requireViewModel(
    key: String? = null,
): T {
    return requireViewModel(T::class.java, key)
}