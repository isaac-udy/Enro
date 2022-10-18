package dev.enro.core.compose.destination

import android.os.Bundle
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory
import dagger.hilt.internal.GeneratedComponentManagerHolder
import dev.enro.core.addOpenInstruction
import dev.enro.core.controller.application
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import dev.enro.viewmodel.EnroViewModelFactory

internal class ComposableDestinationViewModelStoreOwner(
    private val owner: ComposableDestinationOwner,
    private val savedState: Bundle
): ViewModelStoreOwner,
    HasDefaultViewModelProviderFactory {

    private val viewModelStore: ViewModelStore = ViewModelStore()

    init {
        owner.enableSavedStateHandles()
        owner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if(event != Lifecycle.Event.ON_DESTROY) return
                viewModelStore.clear()
            }
        })
    }

    override fun getViewModelStore(): ViewModelStore {
        return viewModelStore
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
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

        return EnroViewModelFactory(
            getNavigationHandleViewModel(),
            factory
        )
    }

    override fun getDefaultViewModelCreationExtras(): CreationExtras {
        return MutableCreationExtras().apply {
            set(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY, owner.navigationController.application)
            set(SAVED_STATE_REGISTRY_OWNER_KEY, owner)
            set(VIEW_MODEL_STORE_OWNER_KEY, owner)
        }
    }
}