package dev.enro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass

private fun viewModelNotFoundError(context: NavigationContext, modelClass: KClass<*>): Nothing {
    error("ViewModel ${modelClass.simpleName} was not found in NavigationContext ${context.id}")
}

/**
 * Attempt to get a ViewModel of a certain type from a NavigationContext.
 *
 * @return The ViewModel requested, or null if the ViewModel does not exist in the NavigationContext's ViewModelStore
 */
public fun <T : ViewModel> NavigationContext.getViewModel(
    cls: KClass<T>,
    key: String? = null,
): T? {
    val lazy = ViewModelLazy<T>(
        viewModelClass = cls,
        storeProducer = { viewModelStore },
        factoryProducer = { viewModelNotFoundError(this, cls) },
        extrasProducer = { CreationExtras.Empty },
    )
    val result = kotlin.runCatching { lazy.value }
    return result.getOrNull()
}

/**
 * Attempt to get a ViewModel of a certain type from a NavigationContext.
 *
 * @return The ViewModel requested
 *
 * @throws IllegalStateException if the ViewModel does not already exist in the NavigationContext
 */
public fun <T : ViewModel> NavigationContext.requireViewModel(
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
public inline fun <reified T : ViewModel> NavigationContext.getViewModel(
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
public inline fun <reified T : ViewModel> NavigationContext.requireViewModel(
    key: String? = null,
): T {
    return requireViewModel(T::class, key)
}