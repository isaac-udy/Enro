package dev.enro.core.internal.handle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.core.AnyOpenInstruction
import androidx.lifecycle.viewmodel.CreationExtras
import dev.enro.core.EnroException
import dev.enro.core.controller.NavigationController

internal class NavigationHandleViewModelFactory(
    private val navigationController: NavigationController,
    private val instruction: AnyOpenInstruction
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return create(modelClass, CreationExtras.Empty)
    }

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if(navigationController.isInTest) {
            return TestNavigationHandleViewModel(
                navigationController,
                instruction
            ) as T
        }

        return NavigationHandleViewModel(
            navigationController,
            instruction
        ) as T
    }
}

internal fun ViewModelStoreOwner.createNavigationHandleViewModel(
    navigationController: NavigationController,
    instruction: AnyOpenInstruction
): NavigationHandleViewModel {
    return ViewModelLazy(
        viewModelClass = NavigationHandleViewModel::class,
        storeProducer = { viewModelStore },
        factoryProducer = { NavigationHandleViewModelFactory(navigationController, instruction) }
    ).value
}

internal class ExpectExistingNavigationHandleViewModelFactory(
    private val viewModelStoreOwner: ViewModelStoreOwner
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val name = viewModelStoreOwner::class.java.simpleName
        throw EnroException.NoAttachedNavigationHandle(
            "Attempted to get the NavigationHandle for $name, but $name not have a NavigationHandle attached."
        )
    }

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val name = viewModelStoreOwner::class.java.simpleName
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