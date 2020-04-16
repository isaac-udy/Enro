package nav.enro.core.internal.handle

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import nav.enro.core.internal.context.FragmentContext
import nav.enro.core.NavigationKey

internal object NavigationHandleFragmentBinder: FragmentManager.FragmentLifecycleCallbacks() {
    override fun onFragmentCreated(fm: FragmentManager, fragment: Fragment, savedInstanceState: Bundle?) {
        val handle = fragment.viewModels<NavigationHandleViewModel<NavigationKey>>().value
        handle.navigationContext = FragmentContext(fragment)
        if(savedInstanceState == null) handle.executeDeeplink()
    }

    override fun onFragmentSaveInstanceState(fm: FragmentManager, fragment: Fragment, outState: Bundle) {}
}