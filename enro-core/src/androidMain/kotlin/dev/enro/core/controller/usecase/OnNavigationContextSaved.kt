package dev.enro.core.controller.usecase

import android.os.Bundle
import dev.enro.core.NavigationContext
import dev.enro.core.getNavigationHandle

internal class OnNavigationContextSaved {
    operator fun invoke(
        context: NavigationContext<*>,
        outState: Bundle
    ) {
        outState.putString(CONTEXT_ID_ARG, context.getNavigationHandle().id)
        context.containerManager.save(outState)
    }
}