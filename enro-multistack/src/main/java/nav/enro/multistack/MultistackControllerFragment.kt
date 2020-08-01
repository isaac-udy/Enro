package nav.enro.multistack

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import nav.enro.core.*
import nav.enro.core.controller.navigationController
import nav.enro.core.executors.DefaultFragmentExecutor
import nav.enro.core.navigator.ActivityNavigator
import nav.enro.core.navigator.FragmentNavigator


@PublishedApi
internal class MultistackControllerFragment : Fragment(), ViewTreeObserver.OnGlobalLayoutListener {

    internal lateinit var containers: Array<out MultistackContainer>

    internal val containerLiveData = MutableLiveData<Int>()

    private var listenForEvents = true
    private lateinit var activeContainer: MultistackContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activeContainer = savedInstanceState?.getParcelable("activecontainer") ?: containers.first()
        requireActivity().findViewById<View>(android.R.id.content)
            .viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        openStack(activeContainer)
        return null // this is a headless fragment
    }

    override fun onStart() {
        super.onStart()
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

    internal fun openStack(container: MultistackContainer) {
        listenForEvents = false
        activeContainer = container
        if(containerLiveData.value != container.containerId) {
            containerLiveData.value = container.containerId
        }

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

        val existingFragment = parentFragmentManager.findFragmentById(container.containerId)
        if (existingFragment != null) {
            val animations = animationsFor(
                existingFragment,
                NavigationInstruction.Open(
                    NavigationDirection.REPLACE, container.rootKey
                )
            )

            if (existingFragment != parentFragmentManager.primaryNavigationFragment) {
                parentFragmentManager.beginTransaction()
                    .setPrimaryNavigationFragment(existingFragment)
                    .commitNow()

                val enter = AnimationUtils.loadAnimation(requireContext(), animations.enter)
                existingFragment.view?.startAnimation(enter)
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

            } catch (ex: Throwable) {
                Handler(Looper.getMainLooper()).post {
                    openStack(container)
                }
            }
        }

        listenForEvents = true
    }

    private fun onStackClosed(container: MultistackContainer) {
        listenForEvents = false
        if (container == containers.first()) {
            requireActivity().getNavigationHandle<Nothing>().close()
        } else {
            openStack(containers.first())
        }
        listenForEvents = true
    }
}