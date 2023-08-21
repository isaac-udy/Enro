package dev.enro.core.controller.lifecycle

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.ComponentDialog
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import dev.enro.core.*
import dev.enro.core.controller.usecase.OnNavigationContextCreated
import dev.enro.core.controller.usecase.OnNavigationContextSaved
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import dev.enro.destination.fragment.FragmentContext
import dev.enro.destination.fragment.navigationContext

internal class FragmentLifecycleCallbacksForEnro(
    private val onNavigationContextCreated: OnNavigationContextCreated,
    private val onNavigationContextSaved: OnNavigationContextSaved,
) : FragmentManager.FragmentLifecycleCallbacks() {
    override fun onFragmentPreCreated(
        fm: FragmentManager,
        fragment: Fragment,
        savedInstanceState: Bundle?
    ) {
        // TODO throw exception if fragment is opened into an Enro registered NavigationContainer without
        // being opened through Enro
        onNavigationContextCreated(FragmentContext(fragment), savedInstanceState)
    }

    override fun onFragmentSaveInstanceState(
        fm: FragmentManager,
        fragment: Fragment,
        outState: Bundle
    ) {
        onNavigationContextSaved(fragment.navigationContext, outState)
    }

    override fun onFragmentViewCreated(
        fm: FragmentManager,
        fragment: Fragment,
        view: View,
        outState: Bundle?
    ) {
        if (fragment is DialogFragment && fragment.showsDialog) {
            val dialog = fragment.requireDialog()
            if (dialog is ComponentDialog) {
                dialog.onBackPressedDispatcher.addCallback(fragment.viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        fragment.navigationContext.leafContext().getNavigationHandle().requestClose()
                    }
                })
            }
            else {
                ViewCompat.addOnUnhandledKeyEventListener(view, DialogFragmentBackPressedListener)
            }
        }
    }
}

private object DialogFragmentBackPressedListener : ViewCompat.OnUnhandledKeyEventListenerCompat {
    override fun onUnhandledKeyEvent(view: View, event: KeyEvent): Boolean {
        val isBackPressed =
            event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP
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