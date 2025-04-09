package dev.enro.core.compose.destination

import android.os.Bundle
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.savedstate.SavedState
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory
import dagger.hilt.internal.GeneratedComponentManagerHolder
import dev.enro.core.activity
import dev.enro.core.addOpenInstruction
import dev.enro.core.controller.application

internal actual fun createViewModelFactory(
    owner: ComposableDestinationOwner,
    savedState: SavedState,
): ViewModelProvider.Factory {
    val activity = owner.parentContainer.context.activity
    val arguments = Bundle().addOpenInstruction(owner.instruction)

    val generatedComponentManagerHolderClass = runCatching {
        GeneratedComponentManagerHolder::class.java
    }.getOrNull()

    return if (generatedComponentManagerHolderClass != null && activity is GeneratedComponentManagerHolder) {
        HiltViewModelFactory.createInternal(
            activity,
            owner,
            arguments,
            SavedStateViewModelFactory(activity.application, owner, savedState)
        )
    } else {
        SavedStateViewModelFactory(activity.application, owner, savedState)
    }
}


internal actual fun MutableCreationExtras.addPlatformExtras(owner: ComposableDestinationOwner) {
    set(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY, owner.navigationController.application)
}