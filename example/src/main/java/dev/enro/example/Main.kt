package dev.enro.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.navigationHandle
import dev.enro.example.databinding.ActivityMainBinding
import dev.enro.multistack.multistackController
import kotlinx.parcelize.Parcelize

@Parcelize
class MainKey : NavigationKey

@NavigationDestination(MainKey::class)
class MainActivity : AppCompatActivity() {

    private val navigation by navigationHandle<MainKey> {
        container(R.id.homeContainer) {
            it is Home || it is SimpleExampleKey
        }
    }

    private val mutlistack by multistackController {
        container(R.id.homeContainer, Home())
        container(R.id.featuresContainer, Features())
        container(R.id.profileContainer, Profile())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            bottomNavigation.setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.home -> mutlistack.openStack(R.id.homeContainer)
                    R.id.features -> mutlistack.openStack(R.id.featuresContainer)
                    R.id.profile -> mutlistack.openStack(R.id.profileContainer)
                    else -> return@setOnNavigationItemSelectedListener false
                }
                return@setOnNavigationItemSelectedListener true
            }

            mutlistack.activeContainer.observe(this@MainActivity, Observer { selectedContainer ->
                bottomNavigation.selectedItemId = when (selectedContainer) {
                    R.id.homeContainer -> R.id.home
                    R.id.featuresContainer -> R.id.features
                    R.id.profileContainer -> R.id.profile
                    else -> 0
                }
            })
        }
    }
}