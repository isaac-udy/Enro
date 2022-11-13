package dev.enro.core.internal.handle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.controller.NavigationController
import dev.enro.core.get

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

        val scope = NavigationHandleScope(
            navigationController
        )
        return NavigationHandleViewModel(
            instruction = instruction,
            dependencyScope = scope,
            executeCloseInstruction = scope.get(),
            executeOpenInstruction = scope.get(),
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