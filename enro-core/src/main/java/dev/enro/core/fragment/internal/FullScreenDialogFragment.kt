package dev.enro.core.fragment.internal

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.core.*
import dev.enro.core.compose.dialog.animate
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.add
import dev.enro.core.container.asPushInstruction
import dev.enro.core.container.createEmptyBackStack
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.internal.getAttributeResourceId
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import kotlinx.parcelize.Parcelize


@Parcelize
internal object FullScreenDialogKey : NavigationKey.SupportsPresent

abstract class AbstractFullscreenDialogFragment : DialogFragment() {
    internal var fragment: Fragment? = null
    internal var animations: NavigationAnimation.Resource? = null

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
                setWindowAnimations(0)
                setBackgroundDrawableResource(android.R.color.transparent)
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FrameLayout(requireContext()).apply {
            id = R.id.enro_internal_single_fragment_frame_layout
            setBackgroundResource(requireActivity().theme.getAttributeResourceId(android.R.attr.windowBackground))
            if(savedInstanceState == null) {
                alpha = 0f
                animate()
                    .setInterpolator(DecelerateInterpolator())
                    .setDuration(100)
                    .alpha(1f)
                    .start()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragment = fragment.also { fragment = null } ?: return
        val animations = animations.also { animations = null }
            ?.asResource(requireActivity().theme)

        childFragmentManager.commitNow {
            attach(fragment)
        }

        view.post {
            childFragmentManager.commitNow {
                if(animations != null) setCustomAnimations(animations.enter, animations.exit)

                add(R.id.enro_internal_single_fragment_frame_layout, fragment, tag)
                setPrimaryNavigationFragment(fragment)
                runOnCommit {
                    container.setBackstack(
                        createEmptyBackStack().add(
                            fragment.getNavigationHandle().instruction.asPushInstruction()
                        )
                    )
                }
            }
        }
    }

    override fun dismiss() {
        val fragment = childFragmentManager.findFragmentById(R.id.enro_internal_single_fragment_frame_layout)
            ?: return super.dismiss()

        val animations = animationsFor(fragment.navigationContext, fragment.getNavigationHandleViewModel().instruction)
        val animationDuration = fragment.requireView().animate(
            animOrAnimator = animations.exit
        )

        val delay = maxOf(0, animationDuration - 75)
        requireView()
            .animate()
            .setInterpolator(AccelerateInterpolator())
            .setStartDelay(delay)
            .setDuration(100)
            .alpha(0f)
            .withEndAction {
                super.dismiss()
            }
            .start()
    }
}

internal class FullscreenDialogFragment : AbstractFullscreenDialogFragment()

@AndroidEntryPoint
internal class HiltFullscreenDialogFragment : AbstractFullscreenDialogFragment()
