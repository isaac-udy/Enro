package dev.enro.core.compose.destination

import android.os.Bundle
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.lifecycle.Lifecycle
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

internal class ComposableDestinationSavedStateRegistryOwner(
    private val owner: ComposableDestinationOwner,
    savedInstanceState: Bundle?
) : SavedStateRegistryOwner {

    private val savedStateController = SavedStateRegistryController.create(this)
    internal val savedState: Bundle = (
            savedInstanceState
                ?: owner.parentSavedStateRegistry.consumeRestoredStateForKey(owner.instruction.instructionId)
                ?: Bundle()
        ).also { savedStateController.performRestore(it) }

    private var restoredComposeState: Map<String, List<Any?>> = savedStateRegistry.consumeRestoredStateForKey("composeState")?.toMap().orEmpty()
    private var activeComposeRegistry: SaveableStateRegistry? = null

    init {
        savedStateController.savedStateRegistry.registerSavedStateProvider("composeState") {
            val activeComposeState = activeComposeRegistry?.performSave().orEmpty()
            val saved = (restoredComposeState + activeComposeState)
            saved.toBundle()
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


@Suppress("DEPRECATION")
private fun Bundle.toMap(): Map<String, List<Any?>>? {
    val map = mutableMapOf<String, List<Any?>>()
    this.keySet().forEach { key ->
        val list = getParcelableArrayList<Parcelable?>(key) as ArrayList<Any?>
        map[key] = list
    }
    return map
}

private fun Map<String, List<Any?>>.toBundle(): Bundle {
    val bundle = Bundle()
    forEach { (key, list) ->
        val arrayList = if (list is ArrayList<Any?>) list else ArrayList(list)
        bundle.putParcelableArrayList(
            key,
            arrayList as ArrayList<Parcelable?>
        )
    }
    return bundle
}