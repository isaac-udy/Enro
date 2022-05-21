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
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.fragment.container.setVisibilityAnimated
import dev.enro.core.navigationHandle
import dev.enro.example.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize

@Parcelize
class MainKey : NavigationKey

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

        containerManager.activeContainerFlow
            .onEach { _ ->
                listOf(
                    homeContainer,
                    featuresContainer,
                    profileContainer,
                ).forEach {
                    it.setVisibilityAnimated(it.isActive)
                }
            }
            .launchIn(lifecycleScope)

        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> homeContainer.setActive()
                R.id.features -> featuresContainer.setActive()
                R.id.profile -> profileContainer.setActive()
                else -> return@setOnItemSelectedListener false
            }
            return@setOnItemSelectedListener true
        }

        if(savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.home
        }
    }
}