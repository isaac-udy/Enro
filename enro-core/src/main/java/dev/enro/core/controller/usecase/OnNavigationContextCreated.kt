package dev.enro.core.controller.usecase

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.core.*
import dev.enro.core.activity.ActivityContext
import dev.enro.core.internal.NoNavigationKey
import dev.enro.core.internal.handle.createNavigationHandleViewModel
import java.util.*

internal const val CONTEXT_ID_ARG = "dev.enro.core.ContextController.CONTEXT_ID"

internal class OnNavigationContextCreated(
    private val configureNavigationHandleForPlugins: ConfigureNavigationHandleForPlugins,
    private val getNavigationExecutor: GetNavigationExecutor,
) {
    operator fun invoke(
        context: NavigationContext<*>,
        savedInstanceState: Bundle?
    ) {
        if (context is ActivityContext) {
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
        val handle = viewModelStoreOwner.createNavigationHandleViewModel(
            context.controller,
            instruction ?: defaultInstruction
        )

        handle.navigationContext = context
        config?.applyTo(context, handle)
        context.containerManager.restore(savedInstanceState)
        configureNavigationHandleForPlugins(context, handle)

        if (savedInstanceState == null) {
            handle.runWhenHandleActive { handle.executeDeeplink() }
            context.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_START) {
                        getNavigationExecutor
                            .forClosing(context)
                            .postOpened(context)
                        context.lifecycle.removeObserver(this)
                    }
                }
            })
        }
    }
}