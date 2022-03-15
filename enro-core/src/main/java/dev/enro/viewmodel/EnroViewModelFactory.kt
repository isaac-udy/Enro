package dev.enro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.enro.core.EnroException
import dev.enro.core.NavigationHandle

@PublishedApi
internal class EnroViewModelFactory(
    private val navigationHandle: NavigationHandle,
    private val delegate: ViewModelProvider.Factory
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        EnroViewModelNavigationHandleProvider.put(modelClass, navigationHandle)
        val viewModel = try {
            delegate.create(modelClass) as T
        } catch (ex: RuntimeException) {
            if(ex is EnroException) throw ex
            throw EnroException.CouldNotCreateEnroViewModel(
                "Failed to created ${modelClass.name} using factory ${delegate::class.java.name}.\n" +
                        "This can occur if you are using an @HiltViewModel annotated ViewModel, but are not requesting the ViewModel from inside an @AndroidEntryPoint annotated Activity/Fragment.",
                ex
            )
        }
        EnroViewModelNavigationHandleProvider.clear(modelClass)
        return viewModel
    }

}