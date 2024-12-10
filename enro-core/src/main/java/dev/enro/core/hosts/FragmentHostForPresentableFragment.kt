package dev.enro.core.hosts

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import androidx.compose.material.ExperimentalMaterialApi
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.core.EnroInternalNavigationKey
import dev.enro.core.NavigationHost
import dev.enro.core.NavigationKey
import dev.enro.core.OpenPresentInstruction
import dev.enro.core.R
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.acceptNone
import dev.enro.core.container.asPushInstruction
import dev.enro.core.container.getAnimationsForEntering
import dev.enro.core.container.getAnimationsForExiting
import dev.enro.core.container.setBackstack
import dev.enro.core.containerManager
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.navigationContext
import dev.enro.core.navigationHandle
import dev.enro.core.parentContainer
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

public abstract class AbstractFragmentHostForPresentableFragment : DialogFragment(), NavigationHost {

    private val navigationHandle by navigationHandle<AbstractOpenPresentableFragmentInFragmentKey>()
    private val container by navigationContainer(
        containerId = R.id.enro_internal_single_fragment_frame_layout,
        emptyBehavior = EmptyBehavior.CloseParent,
        rootInstruction = { navigationHandle.key.instruction.asPushInstruction() },
        filter = acceptNone()
    )
    private val isHostingComposable
        get() = navigationHandle.key.instruction.navigationKey is AbstractOpenComposableInFragmentKey

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = createFullscreenDialog()
    private var isDismissed: Boolean = false

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
        isDismissed = savedInstanceState?.getBoolean(IS_DISMISSED_KEY, false) ?: false
        if (isDismissed) {
            super.dismiss()
            return
        }
        // DialogFragments don't display child animations for fragment transactions correctly
        // if the fragment transaction occurs immediately when the DialogFragment is created,
        // so to solve this issue, we post the animation to the view, which delays this slightly,
        // and ensures the animation occurs correctly
        if (savedInstanceState != null || isHostingComposable) {
            view.alpha = 1f
            return
        }

        fun animateEntry() {
            if (lifecycle.currentState == Lifecycle.State.DESTROYED) return
            val viewLifecycleState = runCatching { viewLifecycleOwner.lifecycle.currentState }
                .getOrNull() ?: Lifecycle.State.INITIALIZED
            if (viewLifecycleState == Lifecycle.State.DESTROYED) return

            val childFragmentManager = runCatching { childFragmentManager }.getOrNull()
            if (childFragmentManager == null) {
                view.post { animateEntry() }
                return
            }
            val fragment = childFragmentManager.findFragmentById(R.id.enro_internal_single_fragment_frame_layout)
            requireNotNull(fragment)

            view.alpha = 1f

            if (fragment is AbstractFragmentHostForComposable) return

            val parentContainer = navigationContext.parentContainer() ?: return
            val animations = parentContainer
                .getAnimationsForEntering(navigationHandle.key.instruction)
                .asResource(fragment.requireActivity().theme)

            view.animate(
                animOrAnimator = animations.id
            )
        }
        view.post {
            animateEntry()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_DISMISSED_KEY, isDismissed)
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun dismiss() {
        val fragment =
            childFragmentManager.findFragmentById(R.id.enro_internal_single_fragment_frame_layout)
                ?: return super.dismiss()

        isDismissed = true

        val parentContainer = navigationContext.parentContainer()
        val animationResource = when {
            fragment is AbstractFragmentHostForComposable -> R.anim.enro_no_op_exit_animation
            parentContainer != null -> {
                parentContainer
                    .getAnimationsForExiting(navigationHandle.key.instruction)
                    .asResource(fragment.requireActivity().theme).id
            }
            else -> R.anim.enro_fallback_exit
        }

        val animationDuration = fragment.view?.animate(animationResource) ?: 0
        if(fragment is NavigationHost) {
            val activeContainer = fragment
                .containerManager
                .activeContainer
            when (
                val activeContextReference = activeContainer
                    ?.childContext
                    ?.contextReference
            ) {
                is ComposableDestination -> activeContainer.setBackstack { emptyList() }
                else -> {}
            }
        }

        val delay = maxOf(0, animationDuration - 16)
        (view ?: return)
            .animate()
            .setInterpolator(AccelerateInterpolator())
            .setStartDelay(delay)
            .setDuration(16)
            .alpha(0f)
            .withEndAction {
                // If the state is not saved, we can dismiss
                // otherwise isDismissed will have been saved into the saved instance state,
                // and this will immediately dismiss after onViewCreated is called next
                if (!isStateSaved) {
                    super.dismiss()
                }
            }
            .start()
    }

    private companion object {
        private val IS_DISMISSED_KEY =
            "AbstractFragmentHostForPresentableFragment.IS_DISMISSED_SAVED_STATE"
    }
}

internal class FragmentHostForPresentableFragment : AbstractFragmentHostForPresentableFragment()

@AndroidEntryPoint
internal class HiltFragmentHostForPresentableFragment : AbstractFragmentHostForPresentableFragment()