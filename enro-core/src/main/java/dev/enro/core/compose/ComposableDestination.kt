package dev.enro.core.compose

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory
import dagger.hilt.internal.GeneratedComponentManagerHolder
import dev.enro.core.*
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import dev.enro.viewmodel.EnroViewModelFactory


internal class ComposableDestinationContextReference(
    val instruction: NavigationInstruction.Open,
    val destination: ComposableDestination,
    internal var parentContainer: EnroContainerController?
) : ViewModel(),
    LifecycleOwner,
    ViewModelStoreOwner,
    HasDefaultViewModelProviderFactory,
    SavedStateRegistryOwner {

    private val navigationController get() = requireParentContainer().navigationContext.controller
    private val parentViewModelStoreOwner get() = requireParentContainer().navigationContext.viewModelStoreOwner
    private val parentSavedStateRegistry get() = requireParentContainer().navigationContext.savedStateRegistryOwner.savedStateRegistry
    internal val activity: FragmentActivity get() = requireParentContainer().navigationContext.activity

    private val arguments by lazy { Bundle().addOpenInstruction(instruction) }
    private val savedState: Bundle? =
        parentSavedStateRegistry.consumeRestoredStateForKey(instruction.instructionId)
    private val savedStateController = SavedStateRegistryController.create(this)
    private val viewModelStore: ViewModelStore = ViewModelStore()


    @SuppressLint("StaticFieldLeak")
    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    private var defaultViewModelFactory: Pair<Int, ViewModelProvider.Factory> =
        0 to ViewModelProvider.NewInstanceFactory()

    init {
        destination.contextReference = this

        savedStateController.performRestore(savedState)
        lifecycleRegistry.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_CREATE -> {
                        parentSavedStateRegistry.registerSavedStateProvider(instruction.instructionId) {
                            val outState = Bundle()
                            navigationController.onComposeContextSaved(
                                destination,
                                outState
                            )
                            savedStateController.performSave(outState)
                            outState
                        }
                        navigationController.onComposeDestinationAttached(
                            destination,
                            savedState
                        )
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        parentSavedStateRegistry.unregisterSavedStateProvider(instruction.instructionId)
                        viewModelStore.clear()
                        lifecycleRegistry.removeObserver(this)
                    }
                    else -> {
                    }
                }
            }
        })
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    override fun getViewModelStore(): ViewModelStore {
        return viewModelStore
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return defaultViewModelFactory.second
    }

    override fun getSavedStateRegistry(): SavedStateRegistry {
        return savedStateController.savedStateRegistry
    }

    internal fun requireParentContainer(): EnroContainerController = parentContainer!!

    @Composable
    private fun rememberDefaultViewModelFactory(navigationHandle: NavigationHandle): Pair<Int, ViewModelProvider.Factory> {
        return remember(parentViewModelStoreOwner.hashCode()) {
            if (parentViewModelStoreOwner.hashCode() == defaultViewModelFactory.first) return@remember defaultViewModelFactory

            val generatedComponentManagerHolderClass = kotlin.runCatching {
                GeneratedComponentManagerHolder::class.java
            }.getOrNull()

            val factory = if (generatedComponentManagerHolderClass != null && activity is GeneratedComponentManagerHolder) {
                HiltViewModelFactory.createInternal(
                    activity,
                    this,
                    arguments,
                    SavedStateViewModelFactory(activity.application, this, savedState)
                )
            } else {
                SavedStateViewModelFactory(activity.application, this, savedState)
            }

            return@remember parentViewModelStoreOwner.hashCode() to EnroViewModelFactory(
                navigationHandle,
                factory
            )
        }
    }

    @Composable
    fun Render() {
        val saveableStateHolder = rememberSaveableStateHolder()
        if (!lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.CREATED)) return

        val backstackState by requireParentContainer().backstack.collectAsState()
        val parentContext = requireParentContainer().navigationContext
        DisposableEffect(true) {
            onDispose {
                if (!backstackState.backstack.contains(instruction)) {
                    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                }
            }
        }

        val isVisible = instruction == backstackState.visible
        val animations = remember(isVisible) {
            if (backstackState.skipAnimations) return@remember DefaultAnimations.none
            animationsFor(
                parentContext,
                backstackState.lastInstruction
            )
        }

        EnroAnimatedVisibility(
            visible = isVisible,
            animations = animations
        ) {
            DisposableEffect(isVisible) {
                if (isVisible) {
                    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
                } else {
                    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
                }
                onDispose {
                    if (isVisible) {
                        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                    } else {
                        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                    }
                }
            }

            val navigationHandle = remember { getNavigationHandleViewModel() }
            defaultViewModelFactory = rememberDefaultViewModelFactory(navigationHandle)

            CompositionLocalProvider(
                LocalLifecycleOwner provides this,
                LocalViewModelStoreOwner provides this,
                LocalSavedStateRegistryOwner provides this,
                LocalNavigationHandle provides navigationHandle
            ) {
                saveableStateHolder.SaveableStateProvider(key = instruction.instructionId) {
                    destination.Render()
                }
            }

            DisposableEffect(true) {
                onDispose {
                    requireParentContainer().onInstructionDisposed(instruction)
                }
            }
        }
    }
}

internal fun getComposableDestinationContext(
    instruction: NavigationInstruction.Open,
    destination: ComposableDestination,
    parentContainer: EnroContainerController?
): ComposableDestinationContextReference {
    return ComposableDestinationContextReference(
        instruction = instruction,
        destination = destination,
        parentContainer = parentContainer
    )
}

abstract class ComposableDestination: LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner,
    HasDefaultViewModelProviderFactory {
    internal lateinit var contextReference: ComposableDestinationContextReference

    override fun getLifecycle(): Lifecycle {
        return contextReference.lifecycle
    }

    override fun getViewModelStore(): ViewModelStore {
        return contextReference.viewModelStore
    }

    override fun getSavedStateRegistry(): SavedStateRegistry {
        return contextReference.savedStateRegistry
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return contextReference.defaultViewModelProviderFactory
    }

    @Composable
    abstract fun Render()
}
