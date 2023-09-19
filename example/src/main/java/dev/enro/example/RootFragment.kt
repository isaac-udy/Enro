package dev.enro.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.container.EmptyBehavior
import dev.enro.destination.activity.containerManager
import dev.enro.destination.fragment.container.FragmentNavigationContainer
import dev.enro.destination.fragment.container.navigationContainer
import dev.enro.destination.fragment.container.setVisibilityAnimated
import dev.enro.destination.fragment.containerManager
import dev.enro.example.databinding.FragmentRootBinding
import dev.enro.example.destinations.compose.ExampleComposable
import dev.enro.example.destinations.fragment.ExampleFragment
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize

@Parcelize
class RootFragment : NavigationKey.SupportsPush

@AndroidEntryPoint
@NavigationDestination(RootFragment::class)
class RootFragmentDestination : Fragment() {

    private val homeContainer by navigationContainer(
        containerId = R.id.homeContainer,
        root = { Home() },
        accept = {
            it is Home || it is ExampleFragment || it is ExampleComposable
        },
        emptyBehavior = EmptyBehavior.CloseParent
    )

    private val featuresContainer by navigationContainer(
        containerId = R.id.featuresContainer,
        root = { Features() },
        emptyBehavior = EmptyBehavior.Action {
            requireView().findViewById<BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.home
            true
        }
    )

    private val backstackContainer by navigationContainer(
        containerId = R.id.profileContainer,
        root = { Backstacks() },
        accept = { false },
        emptyBehavior = EmptyBehavior.Action {
            requireView().findViewById<BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.home
            true
        }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentRootBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentRootBinding.bind(view)
        binding.bottomNavigation.bindContainers(
            R.id.home to homeContainer,
            R.id.features to featuresContainer,
            R.id.backstack to backstackContainer,
        )
        if(savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.home
        }
    }

    private fun BottomNavigationView.bindContainers(
        vararg containers: Pair<Int, FragmentNavigationContainer>
    ) {
        containerManager.activeContainerFlow
            .onEach { _ ->
                val activeContainer = containers.firstOrNull { it.second.isActive }
                    ?: containers.firstOrNull { it.first == selectedItemId}

                containers.forEach {
                    it.second.setVisibilityAnimated(it.second == activeContainer?.second)
                }

                selectedItemId = activeContainer?.first ?: return@onEach
            }
            .launchIn(lifecycleScope)

        setOnItemSelectedListener { item ->
            containers.firstOrNull { it.first == item.itemId }
                ?.second
                ?.setActive()
            return@setOnItemSelectedListener true
        }
    }
}
