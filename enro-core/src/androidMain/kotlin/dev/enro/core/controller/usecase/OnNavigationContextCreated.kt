package dev.enro.core.controller.usecase

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationHandleProperty
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.activity
import dev.enro.core.internal.NoNavigationKey
import dev.enro.core.internal.handle.createNavigationHandleViewModel
import dev.enro.core.readOpenInstruction
import java.util.UUID

internal const val CONTEXT_ID_ARG = "dev.enro.core.ContextController.CONTEXT_ID"

internal class OnNavigationContextCreated(
    private val activeNavigationHandleReference: ActiveNavigationHandleReference,
) {
    operator fun invoke(
        context: NavigationContext<*>,
        savedInstanceState: Bundle?
    ) {
        if (context.contextReference is Activity) {
            context.activity.theme.applyStyle(android.R.style.Animation_Activity, false)
        }

        val instruction = context.arguments.readOpenInstruction()
        val contextId = instruction?.internal?.instructionId
            ?: savedInstanceState?.getString(CONTEXT_ID_ARG)
            ?: UUID.randomUUID().toString()

        val config = NavigationHandleProperty.getPendingConfig(context)
        val defaultKey = config?.defaultKey
            ?: NoNavigationKey(context.contextReference::class.java, context.arguments)
        val defaultInstruction = NavigationInstruction
            .Open.OpenInternal(
                navigationKey = defaultKey,
                navigationDirection = when (defaultKey) {
                    is NavigationKey.SupportsPresent -> NavigationDirection.Present
                    is NavigationKey.SupportsPush -> NavigationDirection.Push
                    else -> NavigationDirection.Present
                }
            )
            .internal
            .copy(instructionId = contextId)

        val viewModelStoreOwner = context.contextReference as ViewModelStoreOwner
        val handle = createNavigationHandleViewModel(
            viewModelStoreOwner = viewModelStoreOwner,
            savedStateRegistryOwner = context.savedStateRegistryOwner,
            navigationController = context.controller,
            instruction = instruction ?: defaultInstruction
        )

        handle.navigationContext = context
        config?.applyTo(context, handle)
        context.containerManager.restore(savedInstanceState)
        activeNavigationHandleReference.watchActiveNavigationHandleFrom(context, handle)

        if (savedInstanceState == null) {
            context.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_START) {
                        context.lifecycle.removeObserver(this)
                    }
                }
            })
        }
    }
}