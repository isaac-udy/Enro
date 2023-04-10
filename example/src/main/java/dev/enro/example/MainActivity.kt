package dev.enro.example

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.navigationHandle
import dev.enro.example.databinding.ActivityMainBinding
import kotlinx.parcelize.Parcelize

@Parcelize
class MainKey : NavigationKey.SupportsPresent

@AndroidEntryPoint
@NavigationDestination(MainKey::class)
class MainActivity : FragmentActivity() {

    private val navigation by navigationHandle {
        defaultKey(MainKey())
    }

    private val rootContainer by navigationContainer(
        containerId = R.id.rootContainer,
        root = { RootFragment() },
        emptyBehavior = EmptyBehavior.CloseParent
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
