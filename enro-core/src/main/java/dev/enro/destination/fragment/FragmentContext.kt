package dev.enro.destination.fragment

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.BackEventCompat
import androidx.activity.ComponentDialog
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationHandle
import dev.enro.core.activity
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.NavigationContainerBackEvent
import dev.enro.core.controller.EnroBackConfiguration
import dev.enro.core.controller.navigationController
import dev.enro.core.getNavigationHandle
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import dev.enro.core.internal.hasKey
import dev.enro.core.isActive
import dev.enro.core.leafContext
import dev.enro.core.navigationContext
import dev.enro.core.parentContainer
import dev.enro.core.requestClose
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

internal fun <ContextType : Fragment> FragmentContext(
    contextReference: ContextType,
): NavigationContext<ContextType> {
    return NavigationContext(
        contextReference = contextReference,
        getController = { contextReference.requireActivity().application.navigationController },
        getParentContext = {
            when (val parentFragment = contextReference.parentFragment) {
                null -> contextReference.requireActivity().navigationContext
                else -> parentFragment.navigationContext
            }
        },
        getArguments = { contextReference.arguments ?: Bundle() },
        getViewModelStoreOwner = { contextReference },
        getSavedStateRegistryOwner = { contextReference },
        getLifecycleOwner = { contextReference },
        onBoundToNavigationHandle = {
            bindBackHandling(this, it)
        }
    )
}

private fun bindBackHandling(navigationContext: NavigationContext<out Fragment>, navigationHandle: NavigationHandle) {
    val backConfiguration = navigationContext.controller.config.backConfiguration

    when(backConfiguration) {
        is EnroBackConfiguration.Default -> configureDefaultBackHandling(navigationContext)
        is EnroBackConfiguration.Manual -> { /* do nothing */ }
        is EnroBackConfiguration.Predictive -> configurePredictiveBackHandling(navigationContext, navigationHandle)
    }
}

private fun configurePredictiveBackHandling(
    navigationContext: NavigationContext<out Fragment>,
    navigationHandle: NavigationHandle
) {
    val activity = navigationContext.activity
    val callback = object : OnBackPressedCallback(false) {
        private var parentContainer: NavigationContainer? = null

        override fun handleOnBackStarted(backEvent: BackEventCompat) {
            parentContainer = navigationContext.parentContainer()
            parentContainer?.backEvents?.tryEmit(NavigationContainerBackEvent.Started(navigationContext))
        }

        override fun handleOnBackProgressed(backEvent: BackEventCompat) {
            parentContainer?.backEvents?.tryEmit(NavigationContainerBackEvent.Progressed(navigationContext, backEvent))
        }

        override fun handleOnBackPressed() {
            parentContainer?.backEvents?.tryEmit(NavigationContainerBackEvent.Confirmed(navigationContext))
            parentContainer = null
        }

        override fun handleOnBackCancelled() {
            parentContainer?.backEvents?.tryEmit(NavigationContainerBackEvent.Cancelled(navigationContext))
            parentContainer = null
        }
    }
    activity.onBackPressedDispatcher.addCallback(navigationContext.lifecycleOwner, callback)

    navigationContext.contextReference.viewLifecycleOwnerLiveData
        .observe(navigationContext.lifecycleOwner) { viewLifecycleOwner ->
            val callbackReference = WeakReference(callback)
            navigationContext.isActive
                .onEach { isActive ->
                    requireNotNull(callbackReference.get()) {
                        "Expected reference to OnBackPressedCallback callback to be non-null, but was null."
                    }.isEnabled = isActive
                }
                .launchIn(viewLifecycleOwner.lifecycleScope)
        }
}

private fun configureDefaultBackHandling(
    navigationContext: NavigationContext<out Fragment>,
) {
    val fragment = navigationContext.contextReference
    fun configure() {
        if (fragment !is DialogFragment || !fragment.showsDialog) return
        if (earlyExitForUnboundFragmentsInTesting(fragment)) return
        val view = fragment.requireView()
        val dialog = fragment.requireDialog()
        when (dialog) {
            is ComponentDialog -> dialog.onBackPressedDispatcher.addCallback(
                fragment.viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        fragment.navigationContext.leafContext().getNavigationHandle()
                            .requestClose()
                    }
                }
            )
            else -> ViewCompat.addOnUnhandledKeyEventListener(
                view,
                DialogFragmentBackPressedListener
            )
        }
    }

    fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.withStarted {
                configure()
            }
        }
    }
}

/**
 * When an unbound DialogFragment is opened, it will get a back press listener bound to it's View,
 * so that it can be integrated with Enro. However, when an EnroTestRule is active,
 * this back press listener will capture Espresso.pressBack instructions and prevent the
 * DialogFragment from closing in situations that it *would* close in a real application; for this
 * reason, we skip adding back press listeners based on this method. This means that an EnroTestRule
 * can't be used to test all the navigation behaviour of an unbound DialogFragment, but this is not
 * really a concern, because if a user wanted to test the Fragment using Enro, the Fragment can
 * be migrated to be a correctly bound Fragment, rather than relying on unbound interoperability
 */
private fun earlyExitForUnboundFragmentsInTesting(
    fragment: Fragment
) : Boolean {
    val hasKey = fragment.getNavigationHandle().hasKey
    val isInTest = fragment.requireActivity().application.navigationController.config.isInTest
    return isInTest && !hasKey
}

private object DialogFragmentBackPressedListener : ViewCompat.OnUnhandledKeyEventListenerCompat {
    override fun onUnhandledKeyEvent(view: View, event: KeyEvent): Boolean {
        val isBackPressed = event.keyCode == KeyEvent.KEYCODE_BACK &&
                event.action == KeyEvent.ACTION_UP

        if (!isBackPressed) return false

        view.findViewTreeViewModelStoreOwner()
            ?.getNavigationHandleViewModel()
            ?.navigationContext
            ?.leafContext()
            ?.getNavigationHandle()
            ?.requestClose() ?: return false

        return true
    }
}
