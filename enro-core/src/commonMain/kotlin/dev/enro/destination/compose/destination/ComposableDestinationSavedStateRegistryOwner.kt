package dev.enro.core.compose.destination

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.lifecycle.Lifecycle
import androidx.savedstate.SavedState
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.read
import androidx.savedstate.savedState
import dev.enro.core.addOpenInstruction

internal class ComposableDestinationSavedStateRegistryOwner(
    private val owner: ComposableDestinationOwner,
    savedInstanceState: SavedState?
) : SavedStateRegistryOwner {

    internal val savedStateController = SavedStateRegistryController.create(this)
    internal val savedState: SavedState = run {
        savedInstanceState
            ?: owner.parentSavedStateRegistry.consumeRestoredStateForKey(owner.instruction.instructionId)
            ?: savedState()
    }
        .also { it.addOpenInstruction(owner.instruction) }
        .also { savedStateController.performRestore(it) }

    private var restoredComposeState: Map<String, List<Any?>> = savedStateRegistry.consumeRestoredStateForKey("composeState")?.toMap().orEmpty()
    private var activeComposeRegistry: SaveableStateRegistry? = null

    init {
        savedStateController.savedStateRegistry.registerSavedStateProvider("composeState") {
            val activeComposeState = activeComposeRegistry?.performSave().orEmpty()
            val saved = (restoredComposeState + activeComposeState)
            saved.toSavedState()
        }
    }

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    override val lifecycle: Lifecycle
        get() = owner.lifecycle

    @Composable
    fun SaveableStateProvider(content: @Composable () -> Unit) {
        val localSaveableStateRegistry = LocalSaveableStateRegistry.current
        val registry = remember {
            if (activeComposeRegistry != null) {
                restoredComposeState = activeComposeRegistry?.performSave().orEmpty()
            }
            val registry = SaveableStateRegistry(
                restoredValues = restoredComposeState,
                canBeSaved = { localSaveableStateRegistry?.canBeSaved(it) ?: false }
            )
            activeComposeRegistry = registry
            return@remember registry
        }

        CompositionLocalProvider(
            LocalSaveableStateRegistry provides registry
        ) {
            content()
        }

        DisposableEffect(Unit) {
            onDispose {
                restoredComposeState = registry.performSave()
                activeComposeRegistry = null
            }
        }
    }
}

private fun SavedState.toMap(): Map<String, List<Any?>> {
    return read { this@read.toMap() }
        .mapValues { (key, value) ->
            if (value is List<*>) return@mapValues value
            else listOf(value)
        }
}

private fun Map<String, List<Any?>>.toSavedState(): SavedState {
    return savedState(this)
}