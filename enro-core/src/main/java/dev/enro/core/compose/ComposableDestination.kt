package dev.enro.core.compose

import android.app.Activity
import android.app.Application
import android.content.ContextWrapper
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationInstruction
import dev.enro.core.internal.handle.getNavigationHandleViewModel


abstract class ComposableDestination: LifecycleOwner, ViewModelStoreOwner {

    internal val lifecycleRegistry: LifecycleRegistry by lazy {  LifecycleRegistry(this) }

    internal lateinit var instruction: NavigationInstruction.Open
    internal lateinit var activity: FragmentActivity
    internal lateinit var containerState: EnroContainerState
    internal lateinit var navigationHandle: NavigationHandle
    internal lateinit var viewModelStoreOwner: ViewModelStoreOwner

    internal var parentContext: NavigationContext<*>? = null

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    override fun getViewModelStore(): ViewModelStore {
        return viewModelStoreOwner.viewModelStore
    }

    @Composable
    internal fun InternalRender() {
        if(lifecycleRegistry.currentState == Lifecycle.State.DESTROYED) return
        navigationHandle = getNavigationHandleViewModel()
        CompositionLocalProvider(
            LocalLifecycleOwner provides this,
            LocalNavigationHandle provides navigationHandle,
            LocalViewModelStoreOwner provides viewModelStoreOwner
        ) {
            Render()
        }
        val containerState = LocalEnroContainerState.current
        DisposableEffect(true) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            onDispose {
                if(containerState.backstack.contains(instruction)) {
                    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                }
                else {
                    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                    containerState.viewModelStoreManager.viewModelStores.remove((viewModelStoreOwner as ComposableDestinationViewModelStoreOwner).id)
                    viewModelStoreOwner.viewModelStore.clear()
                }
            }
        }
    }

    @Composable
    abstract fun Render()
}

internal class ComposableDestinationViewModelStoreOwner(
    val id: String
) : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
    private val viewModelStore = ViewModelStore()
    lateinit var factory: ViewModelProvider.Factory

    override fun getViewModelStore(): ViewModelStore {
        return viewModelStore
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return factory
    }
}

@Composable
internal fun createViewModelStoreOwner(id: String, currentLifecycle: LifecycleOwner): ViewModelStoreOwner {
    val parentSavedState = LocalSavedStateRegistryOwner.current.savedStateRegistry
    val savedState = remember(id) { Bundle() }

    val manager = LocalEnroContainerState.current.viewModelStoreManager
    val viewModelStoreOwner = remember(id) {
        manager.viewModelStores.getOrPut(id) {
            ComposableDestinationViewModelStoreOwner(id)
        } as ComposableDestinationViewModelStoreOwner
    }

    val savedStateRegistryOwner = remember(id) {
        object : SavedStateRegistryOwner, LifecycleEventObserver, ViewModelStoreOwner {
            private val savedStateController = SavedStateRegistryController.create(this)
            private val lifecycleRegistry = LifecycleRegistry(this)

            init {
                savedStateController.performRestore(parentSavedState.consumeRestoredStateForKey(id))
                currentLifecycle.lifecycle.addObserver(this)
            }

            override fun getViewModelStore(): ViewModelStore {
                return viewModelStoreOwner.viewModelStore
            }

            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if(event <= Lifecycle.Event.ON_RESUME) {
                    parentSavedState.unregisterSavedStateProvider(id)
                    parentSavedState.registerSavedStateProvider(id) {
                        savedStateController.performSave(savedState)
                        if (currentLifecycle.lifecycle.currentState <= Lifecycle.State.CREATED) {
                            parentSavedState.unregisterSavedStateProvider(id)
                        }
                        savedState
                    }
                }
                lifecycleRegistry.handleLifecycleEvent(event)
            }
            override fun getLifecycle(): Lifecycle = lifecycleRegistry

            override fun getSavedStateRegistry(): SavedStateRegistry = savedStateController.savedStateRegistry

        }
    }

    val parentOwner = (LocalViewModelStoreOwner.current)
    val parentViewModelFactory = (parentOwner as? HasDefaultViewModelProviderFactory) ?.defaultViewModelProviderFactory
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val defaultFactory = remember(id) {
        if(parentViewModelFactory is HiltViewModelFactory) {
            val activity = context.let {
                var ctx = it
                while (ctx is ContextWrapper) {
                    if (ctx is Activity) {
                        return@let ctx
                    }
                    ctx = ctx.baseContext
                }
                throw IllegalStateException("TODO Exception Details")
            }
            HiltViewModelFactory.createInternal(
                activity,
                savedStateRegistryOwner,
                savedState,
                SavedStateViewModelFactory(application, savedStateRegistryOwner, savedState)
            )
        }
        else {
            SavedStateViewModelFactory(application, savedStateRegistryOwner, savedState)
        }
    }

    return viewModelStoreOwner.apply {
        factory = defaultFactory
    }
}