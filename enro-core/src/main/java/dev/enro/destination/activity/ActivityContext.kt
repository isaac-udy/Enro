package dev.enro.destination.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import dev.enro.core.NavigationContext
import dev.enro.core.controller.navigationController

internal fun <ContextType : ComponentActivity> ActivityContext(
    contextReference: ContextType,
) : NavigationContext<ContextType> {
    return NavigationContext(
        contextReference = contextReference,
        getController = { contextReference.application.navigationController },
        getParentContext = { null },
        getArguments = { contextReference.intent.extras ?: Bundle() },
        getViewModelStoreOwner = { contextReference },
        getSavedStateRegistryOwner = { contextReference },
        getLifecycleOwner = { contextReference },
    )
}