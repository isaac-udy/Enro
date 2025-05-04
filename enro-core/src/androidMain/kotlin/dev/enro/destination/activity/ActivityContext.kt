package dev.enro.destination.activity

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import dev.enro.compatability.interceptBackPressForAndroidxNavigation
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationHandle
import dev.enro.core.controller.EnroBackConfiguration
import dev.enro.core.controller.navigationController
import dev.enro.core.getNavigationHandle
import dev.enro.core.internal.handle.hasCustomOnRequestClose
import dev.enro.core.isActive
import dev.enro.core.leafContext
import dev.enro.core.navigationContext
import dev.enro.core.readOpenInstruction
import dev.enro.core.requestClose
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal fun <ContextType : ComponentActivity> ActivityContext(
    contextReference: ContextType,
): NavigationContext<ContextType> {
    return NavigationContext(
        contextReference = contextReference,
        getController = { contextReference.application.navigationController },
        getParentContext = { null },
        getUnboundChildContext = {
            val fragmentManager = (contextReference as? FragmentActivity)?.supportFragmentManager
            runCatching { fragmentManager?.primaryNavigationFragment?.navigationContext }.getOrNull()
        },
        getContextInstruction = {
            contextReference.intent.readOpenInstruction()
        },
        getViewModelStoreOwner = { contextReference },
        getSavedStateRegistryOwner = { contextReference },
        getLifecycleOwner = { contextReference },
        onBoundToNavigationHandle = {
            bindBackHandling(this, it)
        }
    )
}

private fun bindBackHandling(navigationContext: NavigationContext<out ComponentActivity>, navigationHandle: NavigationHandle) {
    val backConfiguration = navigationContext.controller.config.backConfiguration

    when (backConfiguration) {
        is EnroBackConfiguration.Default -> configureDefaultBackHandling(navigationContext)
        is EnroBackConfiguration.Manual -> { /* do nothing */
        }

        is EnroBackConfiguration.Predictive -> configurePredictiveBackHandling(navigationContext, navigationHandle)
    }
}

private fun configureDefaultBackHandling(
    navigationContext: NavigationContext<out ComponentActivity>,
) {
    val activity = navigationContext.contextReference
    activity.onBackPressedDispatcher.addCallback(activity) {
        val leafContext = navigationContext.leafContext()
        if (interceptBackPressForAndroidxNavigation(this, leafContext)) return@addCallback
        leafContext.getNavigationHandle().requestClose()
    }
}

private fun configurePredictiveBackHandling(
    navigationContext: NavigationContext<out ComponentActivity>,
    navigationHandle: NavigationHandle,
) {
    val activity = navigationContext.contextReference
    val callback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            navigationHandle.requestClose()
        }
    }
    activity.onBackPressedDispatcher.addCallback(activity, callback)
    navigationContext.isActive
        .onEach { isActive ->
            // We're only going to set the callback to be enabled if the navigation handle has a custom close callback,
            // because otherwise we should be happy to fall back through to the default back handling, which
            // will allow predictive back animations to occur
            callback.isEnabled = isActive && navigationHandle.hasCustomOnRequestClose
        }
        .launchIn(activity.lifecycleScope)
}