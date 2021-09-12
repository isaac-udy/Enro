package dev.enro.example

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.navigationContainer
import dev.enro.core.navigationHandle
import dev.enro.example.databinding.ActivityMainBinding
import dev.enro.multistack.multistackController
import kotlinx.parcelize.Parcelize

@Parcelize
class MainKey : NavigationKey

@AndroidEntryPoint
@NavigationDestination(MainKey::class)
class MainActivity : AppCompatActivity() {

    private val homeContainer by navigationContainer(R.id.homeContainer, { Home() },  {
        it is Home || it is SimpleExampleKey || it is ComposeSimpleExampleKey
    })
    private val featuresContainer by navigationContainer(R.id.featuresContainer, { Features() }, { false })

    private val profileContainer by navigationContainer(R.id.profileContainer, { Profile() }, { false })

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
        }
    }
}