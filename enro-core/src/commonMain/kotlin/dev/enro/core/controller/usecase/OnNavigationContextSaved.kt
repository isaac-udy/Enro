package dev.enro.core.controller.usecase

import androidx.savedstate.SavedState
import androidx.savedstate.write
import dev.enro.core.NavigationContext
import dev.enro.core.getNavigationHandle

internal class OnNavigationContextSaved {
    operator fun invoke(
        context: NavigationContext<*>,
        outState: SavedState,
    ) {
        outState.write {
            putString(CONTEXT_ID_ARG, context.getNavigationHandle().id)
        }
        context.containerManager.save(outState)
    }
}