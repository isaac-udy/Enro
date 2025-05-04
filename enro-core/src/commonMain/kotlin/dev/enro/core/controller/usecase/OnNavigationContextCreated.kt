package dev.enro.core.controller.usecase

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedState
import androidx.savedstate.read
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationHandleProperty
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.internal.NoNavigationKey
import dev.enro.core.internal.handle.createNavigationHandleViewModel
import kotlin.uuid.Uuid

internal const val CONTEXT_ID_ARG = "dev.enro.core.ContextController.CONTEXT_ID"

internal class OnNavigationContextCreated(
    private val activeNavigationHandleReference: ActiveNavigationHandleReference,
) {
    operator fun invoke(
        context: NavigationContext<*>,
        savedInstanceState: SavedState?
    ) {
        val instruction = context.contextInstruction
        val contextId = instruction?.instructionId
            ?: savedInstanceState?.read { getStringOrNull(CONTEXT_ID_ARG) }
            ?: Uuid.random().toString()

        val config = NavigationHandleProperty.getPendingConfig(context)
        val defaultKey = config?.defaultKey
            ?: NoNavigationKey(context.contextReference::class.qualifiedName ?: "UnknownContextType")

        val defaultDirection: NavigationDirection = when (defaultKey) {
            is NavigationKey.SupportsPresent -> NavigationDirection.Present
            is NavigationKey.SupportsPush -> NavigationDirection.Push
            else -> NavigationDirection.Present
        }
        val defaultInstruction = NavigationInstruction
            .Open(
                navigationKey = defaultKey,
                navigationDirection = defaultDirection,
            )
            .copy(instructionId = contextId)

        val handle = createNavigationHandleViewModel(
            viewModelStoreOwner = context.viewModelStoreOwner,
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