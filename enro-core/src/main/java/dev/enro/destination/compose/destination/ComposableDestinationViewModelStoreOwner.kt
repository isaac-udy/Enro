package dev.enro.destination.compose.destination

import android.os.Bundle
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory
import dagger.hilt.internal.GeneratedComponentManagerHolder
import dev.enro.core.addOpenInstruction
import dev.enro.core.controller.application
import dev.enro.core.getNavigationHandle
import dev.enro.viewmodel.withNavigationHandle

internal class ComposableDestinationViewModelStoreOwner(
    private val owner: ComposableDestinationOwner,
    private val savedState: Bundle,
    override val viewModelStore: ViewModelStore,
): ViewModelStoreOwner,
    HasDefaultViewModelProviderFactory {

    init {
        owner.enableSavedStateHandles()
    }

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory get() {
        val activity = owner.activity
        val arguments =  Bundle().addOpenInstruction(owner.instruction)

        val generatedComponentManagerHolderClass = kotlin.runCatching {
            GeneratedComponentManagerHolder::class.java
        }.getOrNull()

        val factory = if (generatedComponentManagerHolderClass != null && activity is GeneratedComponentManagerHolder) {
            HiltViewModelFactory.createInternal(
                activity,
                owner,
                arguments,
                SavedStateViewModelFactory(activity.application, owner, savedState)
            )
        } else {
            SavedStateViewModelFactory(activity.application, owner, savedState)
        }

        return factory.withNavigationHandle(getNavigationHandle())
    }

    override val defaultViewModelCreationExtras: CreationExtras get() {
        return MutableCreationExtras().apply {
            set(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY, owner.navigationController.application)
            set(SAVED_STATE_REGISTRY_OWNER_KEY, owner)
            set(VIEW_MODEL_STORE_OWNER_KEY, owner)
        }
    }
}