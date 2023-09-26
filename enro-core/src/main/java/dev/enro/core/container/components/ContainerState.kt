package dev.enro.core.container.components

import android.os.Bundle
import android.os.Looper
import androidx.annotation.MainThread
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import dev.enro.compatability.Compatibility
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.EnroException
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationBackstackTransition
import dev.enro.core.container.emptyBackstack
import dev.enro.core.container.ensureOpeningTypeIsSet
import dev.enro.core.container.toBackstack
import dev.enro.extensions.getParcelableListCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val BACKSTACK_KEY = "NavigationContainer.BACKSTACK_KEY"

public class ContainerState(
    private val key: NavigationContainerKey,
    private val context: NavigationContext<*>,
    initialBackstack: NavigationBackstack,
    private val savables: List<Savable>,
    private val acceptPolicy: ContainerAcceptPolicy,
    private val emptyPolicy: ContainerEmptyPolicy,
    private val activePolicy: ContainerActivePolicy,
) {

    private val mutableBackstackFlow: MutableStateFlow<NavigationBackstack> =
        MutableStateFlow(initialBackstack)

    public val backstackFlow: StateFlow<NavigationBackstack> get() = mutableBackstackFlow

    // snapshotFlow {  } on this for the backstack flow?
    private var mutableBackstack by mutableStateOf(initialBackstack)
    public val backstack: NavigationBackstack by derivedStateOf { mutableBackstack }

    public var currentTransition: NavigationBackstackTransition = initialTransition

    init {
        restoreOrSetBackstack(initialBackstack)
    }

    // move this to a lifecycle manager?
    private fun restoreOrSetBackstack(backstack: NavigationBackstack) {
        val savedStateRegistry = context.savedStateRegistryOwner.savedStateRegistry

        savedStateRegistry.unregisterSavedStateProvider(key.name)
        savedStateRegistry.registerSavedStateProvider(key.name) { save() }

        val initialise = {
            val savedState = savedStateRegistry.consumeRestoredStateForKey(key.name)
            when (savedState) {
                null -> setBackstack(backstack)
                else -> restore(savedState)
            }
        }
        if (!savedStateRegistry.isRestored) {
            context.lifecycleOwner.lifecycleScope.launch {
                context.lifecycle.withCreated {
                    initialise()
                }
            }
        } else initialise()
    }

    public fun save(): Bundle {
        val out = bundleOf(
            BACKSTACK_KEY to ArrayList(backstack)
        )
        savables.forEach { out.putAll(it.save()) }
        return out
    }

    public fun restore(bundle: Bundle) {
        savables.forEach { it.restore(bundle) }

        val restoredBackstack = bundle.getParcelableListCompat<AnyOpenInstruction>(
            BACKSTACK_KEY
        )
            .orEmpty()
            .toBackstack()

        setBackstack(restoredBackstack)
    }

    @MainThread
    public fun setBackstack(backstack: NavigationBackstack) {
        if (Looper.myLooper() != Looper.getMainLooper()) throw EnroException.NavigationContainerWrongThread(
            "A NavigationContainer's setBackstack method must only be called from the main thread"
        )
        if (backstack == backstackFlow.value) return
        val processedBackstack = Compatibility.NavigationContainer
            .processBackstackForDeprecatedInstructionTypes(backstack, acceptPolicy::accepts)
            .ensureOpeningTypeIsSet(context)
            .processBackstackForPreviouslyActiveContainer()

        if (emptyPolicy.handleEmptyBehaviour(processedBackstack)) return
        val lastBackstack = mutableBackstack
        mutableBackstack = processedBackstack
        mutableBackstackFlow.value = mutableBackstack
        val transition = NavigationBackstackTransition(lastBackstack to processedBackstack)
        activePolicy.setActiveContainerFrom(transition)

        currentTransition = transition
    }

    private fun NavigationBackstack.processBackstackForPreviouslyActiveContainer(): NavigationBackstack {
        return map {
            if (it.internal.previouslyActiveContainer != null) return@map it
            it.internal.copy(
                previouslyActiveContainer = context.containerManager.activeContainer?.key
            )
        }.toBackstack()
    }

    public interface Savable {
        public fun save(): Bundle
        public fun restore(bundle: Bundle)
    }

    public companion object {
        internal val initialBackstack = emptyBackstack()
        internal val initialTransition = NavigationBackstackTransition(initialBackstack to initialBackstack)
    }
}