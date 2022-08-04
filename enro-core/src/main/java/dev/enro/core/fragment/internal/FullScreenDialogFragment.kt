package dev.enro.core.fragment.internal

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.core.*
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.asPushInstruction
import dev.enro.core.container.createEmptyBackStack
import dev.enro.core.container.add
import dev.enro.core.fragment.container.navigationContainer
import kotlinx.parcelize.Parcelize

@Parcelize
internal object FullScreenDialogKey : NavigationKey.SupportsPresent

abstract class AbstractFullscreenDialogFragment : DialogFragment() {
    internal var fragment: Fragment? = null

    private val navigation by navigationHandle<FullScreenDialogKey> { defaultKey(FullScreenDialogKey) }
    private val container by navigationContainer(
        containerId = R.id.enro_internal_single_fragment_frame_layout,
        emptyBehavior = EmptyBehavior.CloseParent,
        accept = { false }
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val theme = requireActivity().packageManager.getActivityInfo(requireActivity().componentName, 0).themeResource
        setStyle(STYLE_NO_FRAME, theme)
        return super.onCreateDialog(savedInstanceState).apply {
            setCanceledOnTouchOutside(false)
            window!!.apply {
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FrameLayout(requireContext()).apply {
            id = R.id.enro_internal_single_fragment_frame_layout
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragment?.let {
            fragment = null
            childFragmentManager.commitNow {
                add(R.id.enro_internal_single_fragment_frame_layout, it, tag)
                setPrimaryNavigationFragment(it)
                runOnCommit {
                    container.setBackstack(createEmptyBackStack().add(it.requireArguments().readOpenInstruction()!!.asPushInstruction()))
                }
            }
        }
    }
}

internal class FullscreenDialogFragment : AbstractFullscreenDialogFragment()

@AndroidEntryPoint
internal class HiltFullscreenDialogFragment : AbstractFullscreenDialogFragment()
