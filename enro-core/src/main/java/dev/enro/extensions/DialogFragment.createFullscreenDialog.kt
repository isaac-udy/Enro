package dev.enro.extensions

import android.app.Dialog
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.ComponentDialog
import androidx.fragment.app.DialogFragment

internal fun DialogFragment.createFullscreenDialog(): Dialog {
    setStyle(DialogFragment.STYLE_NO_FRAME, requireActivity().themeResourceId)
    return ComponentDialog(requireContext(), theme).apply {
        setCanceledOnTouchOutside(false)

        requireNotNull(window).apply {
            setWindowAnimations(0)
            setBackgroundDrawableResource(android.R.color.transparent)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }
}