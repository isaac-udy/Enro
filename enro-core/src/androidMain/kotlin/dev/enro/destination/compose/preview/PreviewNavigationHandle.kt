package dev.enro.core.compose.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.EnroException
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.compose.LocalNavigationHandle
import dev.enro.core.controller.EnroDependencyScope
import dev.enro.core.controller.NavigationController
import dev.enro.core.internal.handle.NavigationHandleScope

internal class PreviewNavigationHandle(
    override val instruction: AnyOpenInstruction,
    private val lifecycleOwner: LifecycleOwner,
) : NavigationHandle {
    override val id: String = instruction.instructionId
    override val key: NavigationKey = instruction.navigationKey
    override val dependencyScope: EnroDependencyScope = NavigationHandleScope(
        navigationController = NavigationController(),
        savedStateHandle = SavedStateHandle(),
    ).bind(this)

    override fun executeInstruction(navigationInstruction: NavigationInstruction) {

    }

    override val lifecycle: Lifecycle get() {
        return lifecycleOwner.lifecycle
    }
}

/**
 * Provides a [PreviewNavigationHandle] in the context of the [content] parameter. This is useful for writing @Preview functions
 * for Composables which require a NavigationHandle to be present.
 */
@Composable
public fun <T : NavigationKey> EnroPreview(
    navigationKey: T,
    navigationDirection: NavigationDirection? = null,
    content: @Composable () -> Unit
) {
    val isValidPreview = LocalInspectionMode.current && LocalNavigationHandle.current == null
    if (!isValidPreview) {
        throw EnroException.ComposePreviewException(
            "EnroPreview can only be used when LocalInspectionMode.current is true (i.e. inside of an @Preview function) and when there is no LocalNavigationHandle already"
        )
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    CompositionLocalProvider(
        LocalNavigationHandle provides PreviewNavigationHandle(
            instruction = when(navigationDirection) {
                NavigationDirection.Push -> NavigationInstruction.Push(navigationKey as NavigationKey.SupportsPush)
                NavigationDirection.Present -> NavigationInstruction.Present(navigationKey as NavigationKey.SupportsPresent)
                else -> NavigationInstruction.DefaultDirection(navigationKey)
            },
            lifecycleOwner = lifecycleOwner
        )
    ) {
        content()
    }
}