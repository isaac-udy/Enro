package nav.enro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import nav.enro.core.NavigationHandle

@PublishedApi
internal class EnroViewModelFactory(
    private val navigationHandle: NavigationHandle<*>,
    private val delegate: ViewModelProvider.Factory
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        EnroViewModelNavigationHandleProvider.put(modelClass, navigationHandle)
        val viewModel = delegate.create(modelClass)
        EnroViewModelNavigationHandleProvider.clear(modelClass)
        return viewModel
    }

}