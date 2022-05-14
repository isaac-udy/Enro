package dev.enro.multistack

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import dev.enro.core.DefaultAnimations
import dev.enro.core.NavigationInstruction
import dev.enro.core.activity.ActivityNavigator
import dev.enro.core.close
import dev.enro.core.controller.navigationController
import dev.enro.core.fragment.DefaultFragmentExecutor
import dev.enro.core.fragment.FragmentNavigator
import dev.enro.core.getNavigationHandle


@PublishedApi
internal class MultistackControllerFragment : Fragment(), ViewTreeObserver.OnGlobalLayoutListener {

    internal lateinit var containers: Array<out MultistackContainer>
    @AnimRes internal var openStackAnimation: Int? = null

    internal val containerLiveData = MutableLiveData<Int>()

    private var listenForEvents = true
    private var containerInitialised = false
    private lateinit var activeContainer: MultistackContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activeContainer = savedInstanceState?.getParcelable("activecontainer") ?: containers.first()
        containerInitialised = savedInstanceState?.getBoolean("containerInitialised", false) ?: false
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("activecontainer", activeContainer)
        outState.putBoolean("containerInitialised", containerInitialised)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().findViewById<View>(android.R.id.content)
            .viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        if (!listenForEvents) return
        if (!containerInitialised) return
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

        if(navigator is ActivityNavigator<*, *>) {
            listenForEvents = true
            return
        }

        navigator as FragmentNavigator<*, *>
        containers.forEach {
            requireActivity().findViewById<View>(it.containerId).isVisible = it.containerId == container.containerId
        }

        val activeContainer = requireActivity().findViewById<View>(container.containerId)
        val existingFragment = parentFragmentManager.findFragmentById(container.containerId)
        if (existingFragment != null) {
            if (existingFragment != parentFragmentManager.primaryNavigationFragment) {
                parentFragmentManager.beginTransaction()
                    .setPrimaryNavigationFragment(existingFragment)
                    .commitNow()
            }

            containerInitialised = true
        } else {
            val instruction = NavigationInstruction.Push(container.rootKey)
            val newFragment = DefaultFragmentExecutor.createFragment(
                parentFragmentManager,
                navigator,
                instruction
            )
            try {
                parentFragmentManager.executePendingTransactions()
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(0, 0)
                    .replace(container.containerId, newFragment, instruction.instructionId)
                    .setPrimaryNavigationFragment(newFragment)
                    .commitNow()

                containerInitialised = true
            } catch (ex: Throwable) {
                Log.e("Enro Mutlistack", "Initial open failed", ex)
                Handler(Looper.getMainLooper()).post {
                    openStack(container)
                }
            }
        }

        val animation = openStackAnimation ?: DefaultAnimations.replace.asResource(requireActivity().theme).enter
        val enter = AnimationUtils.loadAnimation(requireContext(), animation)
        activeContainer.startAnimation(enter)

        listenForEvents = true
    }

    private fun onStackClosed(container: MultistackContainer) {
        listenForEvents = false
        if (container == containers.first()) {
            requireActivity().getNavigationHandle().close()
        } else {
            openStack(containers.first())
        }
        listenForEvents = true
    }
}