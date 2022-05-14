package dev.enro.example

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.annotations.NavigationDestination
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.NavigationKey
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.navigationHandle
import dev.enro.example.databinding.ActivityMainBinding
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

        binding.apply {
            bottomNavigation.setOnNavigationItemSelectedListener {
                val homeView = findViewById<View>(R.id.homeContainer).apply { isVisible = false }
                val featuresView = findViewById<View>(R.id.featuresContainer).apply { isVisible = false }
                val profileView = findViewById<View>(R.id.profileContainer).apply { isVisible = false }
                when (it.itemId) {
                    R.id.home -> {
                        homeView.isVisible = true
                    }
                    R.id.features -> {
                        featuresView.isVisible = true
                    }
                    R.id.profile -> {
                        profileView.isVisible = true
                    }
                    else -> return@setOnNavigationItemSelectedListener false
                }
                return@setOnNavigationItemSelectedListener true
            }
            if(savedInstanceState == null) {
                bottomNavigation.selectedItemId = R.id.home
            }
        }
    }
}