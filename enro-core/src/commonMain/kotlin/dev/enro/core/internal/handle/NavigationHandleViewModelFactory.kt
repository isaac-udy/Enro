package dev.enro.core.internal.handle

import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.savedstate.SavedStateRegistryOwner
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.EnroException
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.get
import kotlin.reflect.KClass

internal class NavigationHandleViewModelFactory(
    private val navigationController: NavigationController,
    private val instruction: AnyOpenInstruction
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        if (navigationController.config.isInTest) {
            return TestNavigationHandleViewModel(
                navigationController,
                instruction
            ) as T
        }

        val scope = NavigationHandleScope(
            navigationController = navigationController,
            savedStateHandle = SavedStateHandle(), //extras.createSavedStateHandle(),
        )
        return NavigationHandleViewModel(
            instruction = instruction,
            dependencyScope = scope,
            executeCloseInstruction = scope.get(),
            executeOpenInstruction = scope.get(),
            executeContainerOperationInstruction = scope.get(),
        ) as T
    }
}

internal fun createNavigationHandleViewModel(
    viewModelStoreOwner: ViewModelStoreOwner,
    savedStateRegistryOwner: SavedStateRegistryOwner,
    navigationController: NavigationController,
    instruction: AnyOpenInstruction
): NavigationHandleViewModel {
    return ViewModelLazy(
        viewModelClass = NavigationHandleViewModel::class,
        storeProducer = { viewModelStoreOwner.viewModelStore },
        factoryProducer = { NavigationHandleViewModelFactory(navigationController, instruction) },
        extrasProducer = {
            MutableCreationExtras().apply {
                set(SAVED_STATE_REGISTRY_OWNER_KEY, savedStateRegistryOwner)
                set(VIEW_MODEL_STORE_OWNER_KEY, viewModelStoreOwner)
            }
        }
    ).value
}

internal class ExpectExistingNavigationHandleViewModelFactory(
    private val viewModelStoreOwner: ViewModelStoreOwner
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        val name = viewModelStoreOwner::class.simpleName
        throw EnroException.NoAttachedNavigationHandle(
            "Attempted to get the NavigationHandle for $name, but $name not have a NavigationHandle attached."
        )
    }
}

internal fun ViewModelStoreOwner.getNavigationHandleViewModel(): NavigationHandleViewModel {
    return ViewModelLazy(
        viewModelClass = NavigationHandleViewModel::class,
        storeProducer = { viewModelStore },
        factoryProducer = {
            ExpectExistingNavigationHandleViewModelFactory(this)
        }
    ).value
}