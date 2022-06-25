package dev.enro.core.compose.preview

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import dev.enro.core.*
import dev.enro.core.compose.LocalNavigationHandle
import dev.enro.core.controller.NavigationController

internal class PreviewNavigationHandle(
    override val instruction: AnyOpenInstruction
) : NavigationHandle {
    override val id: String = instruction.instructionId
    override val key: NavigationKey = instruction.navigationKey

    override val controller: NavigationController = NavigationController()
    override val additionalData: Bundle = Bundle.EMPTY

    private val lifecycleRegistry by lazy {
        LifecycleRegistry(this).apply {
            handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
    }

    override fun executeInstruction(navigationInstruction: NavigationInstruction) {

    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}

@Composable
fun <T : NavigationKey> EnroPreview(
    navigationKey: T,
    content: @Composable () -> Unit
) {
    val isValidPreview = LocalInspectionMode.current && LocalNavigationHandle.current == null
    if (!isValidPreview) {
        throw EnroException.ComposePreviewException(
            "EnroPreview can only be used when LocalInspectionMode.current is true (i.e. inside of an @Preview function) and when there is no LocalNavigationHandle already"
        )
    }
    CompositionLocalProvider(
        LocalNavigationHandle provides PreviewNavigationHandle(NavigationInstruction.DefaultDirection(navigationKey))
    ) {
        content()
    }
}