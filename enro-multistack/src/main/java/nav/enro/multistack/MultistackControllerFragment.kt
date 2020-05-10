package nav.enro.multistack

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import nav.enro.core.*
import nav.enro.core.controller.navigationController
import nav.enro.core.executors.DefaultFragmentExecutor
import nav.enro.core.navigator.ActivityNavigator
import nav.enro.core.navigator.FragmentNavigator
import nav.enro.core.navigator.animationsFor
import java.lang.Exception


@PublishedApi
internal class MultiStackControllerFragment : Fragment(), ViewTreeObserver.OnGlobalLayoutListener {

    private val containers: Array<MultiStackContainer> by lazy {
        requireArguments().getParcelableArray("containers")
                as Array<MultiStackContainer>
    }

    private var listenForEvents = true
    private lateinit var activeContainer: MultiStackContainer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return null // this is a headless fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activeContainer = savedInstanceState?.getParcelable("activecontainer") ?: containers.first()
        requireActivity().findViewById<View>(android.R.id.content)
            .viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        openStack(activeContainer)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("activecontainer", activeContainer)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().findViewById<View>(android.R.id.content)
            .viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        if (!listenForEvents) return
        val isCurrentClosing =
            parentFragmentManager.findFragmentById(activeContainer.containerId) == null
        if (isCurrentClosing) {
            onStackClosed(activeContainer)
            return
        }

        val newActive = containers.firstOrNull() {
            requireActivity().findViewById<View>(it.containerId).isVisible && it.containerId != activeContainer.containerId
        } ?: return

        openStack(newActive)
    }

    private fun openStack(container: MultiStackContainer) {
        listenForEvents = false
        activeContainer = container

        val controller = requireActivity().application.navigationController
        val navigator = controller.navigatorForKeyType(container.rootKey::class)

        if(navigator is ActivityNavigator<*,*>) {

            listenForEvents = true
            return
        }

        navigator as FragmentNavigator<*, *>
        containers.forEach {
            requireActivity().findViewById<View>(it.containerId).isVisible = it == container
        }

        val animations = navigator.animationsFor(
            requireActivity().theme,
            NavigationInstruction.Open(
                NavigationDirection.REPLACE, container.rootKey
            )
        )

        val existingFragment = parentFragmentManager.findFragmentById(container.containerId)
        if (existingFragment != null) {
            if (existingFragment != parentFragmentManager.primaryNavigationFragment) {
                parentFragmentManager.beginTransaction()
                    .setPrimaryNavigationFragment(existingFragment)
                    .commitNow()

                val enter = AnimationUtils.loadAnimation(requireContext(), animations.enter)
                existingFragment.requireView().startAnimation(enter)
            }
        } else {
            val newFragment = DefaultFragmentExecutor.createFragment(
                parentFragmentManager,
                navigator,
                NavigationInstruction.Open(
                    NavigationDirection.FORWARD, container.rootKey
                )
            )
            try {
                parentFragmentManager.executePendingTransactions()
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(0, 0)
                    .replace(container.containerId, newFragment)
                    .setPrimaryNavigationFragment(newFragment)
                    .commitNow()

                newFragment.requireView().alpha = 0.0f
                Handler(Looper.getMainLooper()).post {
                    newFragment.requireView().alpha = 1.0f
                    val enter = AnimationUtils.loadAnimation(requireContext(), animations.enter)
                    newFragment.requireView().startAnimation(enter)
                }

            } catch (ex: Throwable) {
                Handler(Looper.getMainLooper()).post {
                    openStack(container)
                }
            }
        }

        listenForEvents = true
    }

    private fun onStackClosed(container: MultiStackContainer) {
        listenForEvents = false
        if (container == containers.first()) {
            requireActivity().navigationHandle<Nothing>().value.close()
        } else {
            openStack(containers.first())
        }
        listenForEvents = true
    }
}