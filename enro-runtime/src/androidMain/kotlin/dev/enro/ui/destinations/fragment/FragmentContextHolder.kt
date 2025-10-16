package dev.enro.ui.destinations.fragment

import androidx.fragment.app.Fragment
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.enro.NavigationKey

@PublishedApi
internal val Fragment.fragmentContextHolder: FragmentContextHolder
    get() {
        return ViewModelProvider
            .create(
                owner = this,
                factory = (this as HasDefaultViewModelProviderFactory).defaultViewModelProviderFactory,
            )
            .get(FragmentContextHolder::class)
    }

@PublishedApi
internal class FragmentContextHolder : ViewModel() {
    @PublishedApi
    internal val navigationHandle: FragmentNavigationHandle<NavigationKey> = FragmentNavigationHandle()
}