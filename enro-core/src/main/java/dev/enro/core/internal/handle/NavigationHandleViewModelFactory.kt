package dev.enro.core.internal.handle

import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.core.NavigationInstruction
import dev.enro.core.controller.NavigationController

internal class NavigationHandleViewModelFactory(
    private val navigationController: NavigationController,
    private val instruction: NavigationInstruction.Open
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
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
    instruction: NavigationInstruction.Open
): NavigationHandleViewModel {
    return when(this) {
        is FragmentActivity -> viewModels<NavigationHandleViewModel> {
            NavigationHandleViewModelFactory(navigationController, instruction)
        }.value
        is Fragment -> viewModels<NavigationHandleViewModel> {
            NavigationHandleViewModelFactory(navigationController, instruction)
        }.value
        else -> throw IllegalArgumentException("ViewModelStoreOwner must be a Fragment or Activity")
    }
}

internal fun ViewModelStoreOwner.getNavigationHandleViewModel(): NavigationHandleViewModel {
    return when(this) {
        is FragmentActivity -> viewModels<NavigationHandleViewModel> { ViewModelProvider.NewInstanceFactory() }.value
        is Fragment -> viewModels<NavigationHandleViewModel> { ViewModelProvider.NewInstanceFactory() }.value
        else -> throw IllegalArgumentException("ViewModelStoreOwner must be a Fragment or Activity")
    }
}