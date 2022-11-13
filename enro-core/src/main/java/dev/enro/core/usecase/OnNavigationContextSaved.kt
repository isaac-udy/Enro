package dev.enro.core.usecase

import android.os.Bundle
import dev.enro.core.NavigationContext

internal interface OnNavigationContextSaved {
    operator fun invoke(
        context: NavigationContext<*>,
        outState: Bundle
    )
}