package dev.enro.example

import androidx.fragment.app.FragmentActivity
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import kotlinx.parcelize.Parcelize

@Parcelize
class SplashScreenKey : NavigationKey

@NavigationDestination(SplashScreenKey::class)
class SplashScreenActivity : FragmentActivity() {

    private val navigation by navigationHandle<SplashScreenKey> {
        defaultKey(SplashScreenKey())
    }

    override fun onResume() {
        super.onResume()
        navigation.replaceRoot(MainKey())
    }
}