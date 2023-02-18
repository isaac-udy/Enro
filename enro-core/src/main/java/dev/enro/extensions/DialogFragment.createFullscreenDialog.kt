package dev.enro.extensions

import android.app.Dialog
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.ComponentDialog
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import dev.enro.core.getNavigationHandle
import dev.enro.core.leafContext
import dev.enro.core.navigationContext
import dev.enro.core.requestClose

internal fun DialogFragment.createFullscreenDialog(): Dialog {
    setStyle(DialogFragment.STYLE_NO_FRAME, requireActivity().themeResourceId)
    return ComponentDialog(requireContext(), theme).apply {
        setCanceledOnTouchOutside(false)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigationContext.leafContext().getNavigationHandle().requestClose()
            }
        })

        requireNotNull(window).apply {
            setWindowAnimations(0)
            setBackgroundDrawableResource(android.R.color.transparent)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }
}