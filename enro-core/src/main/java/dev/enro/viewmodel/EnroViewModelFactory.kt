package dev.enro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.setNavigationHandleTag
import dev.enro.core.EnroException
import dev.enro.core.NavigationHandle

@PublishedApi
internal class EnroViewModelFactory(
    private val navigationHandle: NavigationHandle,
    private val delegate: ViewModelProvider.Factory
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        EnroViewModelNavigationHandleProvider.put(modelClass, navigationHandle)
        val viewModel = try {
            delegate.create(modelClass) as T
        } catch (ex: RuntimeException) {
            if(ex is EnroException) throw ex
            throw EnroException.CouldNotCreateEnroViewModel(
                "Failed to created ${modelClass.name} using factory ${delegate::class.java.name}.\n",
                ex
            )
        }
        viewModel.setNavigationHandleTag(navigationHandle)
        EnroViewModelNavigationHandleProvider.clear(modelClass)
        return viewModel
    }
}