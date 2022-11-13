package dev.enro.core.usecase

import android.os.Bundle
import dev.enro.core.NavigationContext

internal interface OnNavigationContextCreated {
    operator fun invoke(
        context: NavigationContext<*>,
        savedInstanceState: Bundle?
    )
}