package dev.enro.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.isActive
import dev.enro.core.container.setActive
import dev.enro.core.containerManager
import dev.enro.core.navigationHandle
import dev.enro.example.databinding.ActivityMainBinding
import dev.enro.fragment.container.FragmentNavigationContainer
import dev.enro.fragment.container.navigationContainer
import dev.enro.fragment.container.setVisibilityAnimated
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize

@Parcelize
class MainKey : NavigationKey.SupportsPresent

@AndroidEntryPoint
@NavigationDestination(MainKey::class)
class MainActivity : AppCompatActivity() {

    private val homeContainer by navigationContainer(
        containerId = R.id.homeContainer,
        root = { Home() },
        accept = {
            it is Home || it is SimpleExampleKey || it is ComposeSimpleExampleKey
        },
        emptyBehavior = EmptyBehavior.CloseParent
    )
    private val featuresContainer by navigationContainer(
        containerId = R.id.featuresContainer,
        root = { Features() },
        accept = { false },
        emptyBehavior = EmptyBehavior.Action {
            findViewById<BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.home
            true
        }
    )

    private val profileContainer by navigationContainer(
        containerId = R.id.profileContainer,
        root = { Profile() },
        accept = { false },
        emptyBehavior = EmptyBehavior.Action {
            findViewById<BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.home
            true
        }
    )

    private val navigation by navigationHandle<MainKey>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.bindContainers(
            R.id.home to homeContainer,
            R.id.features to featuresContainer,
            R.id.profile to profileContainer,
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
