package nav.enro.viewmodel

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import nav.enro.core.NavigationHandle
import nav.enro.core.getNavigationHandle
import java.lang.IllegalArgumentException

abstract class AbstractNavigationViewModelFactory(
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle?
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    private val navigationHandle by when (owner) {
        is FragmentActivity -> lazy { owner.getNavigationHandle<Nothing>() }
        is Fragment -> lazy { owner.getNavigationHandle<Nothing>() }
        else -> throw IllegalArgumentException("The 'owner' argument for a NavigationViewModelFactory must be a Fragment activity or a Fragment")
    }

    final override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T = create(
        key = key,
        modelClass = modelClass,
        savedStateHandle = handle,
        navigationHandle = navigationHandle
    )

    abstract fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        savedStateHandle: SavedStateHandle,
        navigationHandle: NavigationHandle<*>
    ): T
}