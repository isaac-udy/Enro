package dev.enro.core.hosts

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.core.*
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.asPushInstruction
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import dev.enro.extensions.animate
import dev.enro.extensions.createFullscreenDialog
import dev.enro.extensions.getAttributeResourceId
import kotlinx.parcelize.Parcelize

internal abstract class AbstractOpenPresentableFragmentInFragmentKey : NavigationKey,
    EnroInternalNavigationKey {

    abstract val instruction: OpenPresentInstruction
}

@Parcelize
internal data class OpenPresentableFragmentInFragment(
    override val instruction: OpenPresentInstruction
) : AbstractOpenPresentableFragmentInFragmentKey()

@Parcelize
internal data class OpenPresentableFragmentInHiltFragment(
    override val instruction: OpenPresentInstruction
) : AbstractOpenPresentableFragmentInFragmentKey()

public abstract class AbstractFragmentHostForPresentableFragment : DialogFragment() {

    private val navigationHandle by navigationHandle<AbstractOpenPresentableFragmentInFragmentKey>()
    private val container by navigationContainer(
        containerId = R.id.enro_internal_single_fragment_frame_layout,
        emptyBehavior = EmptyBehavior.CloseParent,
        rootInstruction = { navigationHandle.key.instruction.asPushInstruction() },
        accept = { false }
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = createFullscreenDialog()

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

    override fun dismiss() {
        val fragment = childFragmentManager.findFragmentById(R.id.enro_internal_single_fragment_frame_layout)
            ?: return super.dismiss()

        val animations = animationsFor(fragment.navigationContext, fragment.getNavigationHandleViewModel().instruction)
            .asResource(fragment.requireActivity().theme)
        val animationDuration = fragment.requireView().animate(
            animOrAnimator = animations.exit
        )

        val delay = maxOf(0, animationDuration - 100)
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

internal class FragmentHostForPresentableFragment : AbstractFragmentHostForPresentableFragment()

@AndroidEntryPoint
internal class HiltFragmentHostForPresentableFragment : AbstractFragmentHostForPresentableFragment()