package dev.enro.core.hosts

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FrameLayout(requireContext()).apply {
            id = R.id.enro_internal_single_fragment_frame_layout
            alpha = 0f
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // DialogFragments don't display child animations for fragment transactions correctly
        // if the fragment transaction occurs immediately when the DialogFragment is created,
        // so to solve this issue, we post the animation to the view, which delays this slightly,
        // and ensures the animation occurs correctly
        if (savedInstanceState != null) {
            view.alpha = 1f
            return
        }
        view.post {
            view.alpha = 1f
            val fragment =
                childFragmentManager.findFragmentById(R.id.enro_internal_single_fragment_frame_layout)
            requireNotNull(fragment)

            val animations = animationsFor(
                fragment.navigationContext,
                fragment.getNavigationHandleViewModel().instruction
            )
                .asResource(fragment.requireActivity().theme)

            if (fragment is AbstractFragmentHostForComposable) return@post
            fragment.requireView().animate(
                animOrAnimator = animations.enter
            )
        }
    }

    override fun dismiss() {
        val fragment =
            childFragmentManager.findFragmentById(R.id.enro_internal_single_fragment_frame_layout)
                ?: return super.dismiss()

        val animations = animationsFor(fragment.navigationContext, NavigationInstruction.Close)
            .asResource(fragment.requireActivity().theme)
        val animationDuration = fragment.requireView().animate(
            animOrAnimator = animations.exit
        )

        val delay = maxOf(0, animationDuration - 16)
        requireView()
            .animate()
            .setInterpolator(AccelerateInterpolator())
            .setStartDelay(delay)
            .setDuration(16)
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