package dev.enro.viewmodel

import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import dev.enro.core.EnroException
import dev.enro.core.NavigationHandle
import dev.enro.core.getNavigationHandle
import kotlin.reflect.KClass

internal object EnroViewModelNavigationHandleProvider {
    private val navigationHandles = mutableMapOf<KClass<*>, NavigationHandle>()

    fun put(modelClass: KClass<*>, navigationHandle: NavigationHandle) {
        navigationHandles[modelClass] = navigationHandle
    }

    fun clear(modelClass: KClass<*>) {
        navigationHandles.remove(modelClass)
    }

    fun get(modelClass: KClass<*>): NavigationHandle {
        return navigationHandles[modelClass]
            ?: throw EnroException.ViewModelCouldNotGetNavigationHandle(
                "Could not get a NavigationHandle inside of ViewModel of type ${modelClass.simpleName}. Make sure you are using `by enroViewModels` and not `by viewModels`."
            )
    }

    // Called by enro-test
    fun clearAllForTest() {
        navigationHandles.clear()
    }
}

// TODO added for JS, might need cleanup?
public fun prepareNavigationHandle(
    viewModelType: KClass<out ViewModel>,
    extras: CreationExtras,
) {
    val owner = requireNotNull(extras[VIEW_MODEL_STORE_OWNER_KEY]) {
        "TODO better error"
    }
    EnroViewModelNavigationHandleProvider.put(viewModelType, owner.getNavigationHandle())
}